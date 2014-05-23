/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.collection.DependencyManagement;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.version.Version;

public class DependencyTreeProvider implements TreeProvider<DependencyNodeRequest>
{

   private final ArtifactDescriptorReader descriptorReader;

   private final VersionRangeResolver versionRangeResolver;

   public DependencyTreeProvider(ArtifactDescriptorReader descriptorReader, VersionRangeResolver versionRangeResolver)
   {
      this.descriptorReader = descriptorReader;
      this.versionRangeResolver = versionRangeResolver;
   }

   @Override
   public Collection<DependencyNodeRequest> getChildren(DependencyNodeRequest request)
   {
      final DependencyNodeContext context = request.getContext();
      final Dependency dependency = request.getDependency();

      final DependencyNodeImpl node = collect(context, dependency, null, false);
      if (node == null) // TODO: test relocation not selected
      {
         return Collections.emptyList();
      }

      request.setDependencyNode(node);

      final LinkedList<DependencyNode> parentNodes = context.getParentNodes();
      if (!parentNodes.isEmpty())
      {
         final DependencyNode parentNode = parentNodes.getLast();
         parentNode.getChildren().add(node);
      }

      if (!"ok".equals(node.getData().get("collectionStatus")) || isCyclic(node))
      {
         return Collections.emptyList();
      }

      final ArtifactDescriptorResult descriptorResult = (ArtifactDescriptorResult) node.getData().get(
         "descriptorResult");

      if (Boolean.FALSE.equals(node.getData().get("traverse")))
      {
         return Collections.emptyList();
      }

      // traverse children (if traverse dependencies)
      final List<Dependency> children = context.getDependenciesFilter().filterDependencies(node.getArtifact(),
         descriptorResult.getDependencies());

      if (children.isEmpty())
      {
         return Collections.emptyList();
      }

      final DependencyNodeContext childContext = context.deriveChildContext(node,
         descriptorResult.getManagedDependencies(), descriptorResult.getRepositories());

      final DependencySelector dependencySelector = childContext.getDependencySelector();

      final List<DependencyNodeRequest> childRequests = new ArrayList<DependencyNodeRequest>(children.size());
      for (Dependency child : children)
      {
         // if selectDependency
         if (!dependencySelector.selectDependency(child))
         {
            continue;
         }

         final DependencyNodeRequest childRequest = new DependencyNodeRequest();
         childRequest.setContext(childContext);
         childRequest.setDependency(child);
         childRequests.add(childRequest);
      }

      return childRequests;
   }

   private DependencyNodeImpl collect(DependencyNodeContext context, Dependency dependency, List<Artifact> relocations,
      boolean disableVersionManagement)
   {
      final DependencyNodeImpl node = new DependencyNodeImpl();
      node.setRequestContext(context.getRequestContext());
      node.setDependency(dependency);

      // apply dependency management
      applyDependencyManagement(context, node, disableVersionManagement);

      final Dependency managedDependency = node.getDependency();

      final ArtifactDescriptorResult descriptorResult = resolveAndApplyArtifactDescriptor(context, node);
      if (descriptorResult != null)
      {
         final boolean isCyclic = isCyclic(node);

         // handle relocation
         if (!isCyclic && !node.getRelocations().isEmpty())
         {
            final Artifact originalArtifact = managedDependency.getArtifact();
            final Artifact currentArtifact = node.getArtifact();

            disableVersionManagement = originalArtifact.getGroupId().equals(currentArtifact.getGroupId())
               && originalArtifact.getArtifactId().equals(currentArtifact.getArtifactId());

            final Dependency relocatedDependency = dependency.setArtifact(descriptorResult.getArtifact());
            if (!context.getDependencySelector().selectDependency(relocatedDependency))
            {
               return null;
            }

            return collect(context, relocatedDependency, node.getRelocations(), disableVersionManagement);
         }
      }
      
      node.setRelocations(relocations);
      node.setData("collectionStatus", "ok");

      return node;
   }

   private boolean isCyclic(final DependencyNodeImpl node)
   {
      return node.getData().get("cycleNode") != null;
   }

   private void applyDependencyManagement(DependencyNodeContext context, final DependencyNodeImpl node,
      boolean disableVersionManagement)
   {
      final DependencyManagement dependencyManagement = context.getDependencyManager().manageDependency(
         node.getDependency());
      if (dependencyManagement != null)
      {
         applyDependencyManagement(node, dependencyManagement, context.isSavePremanagedState(),
            disableVersionManagement);
      }
   }

   private ArtifactDescriptorResult resolveAndApplyArtifactDescriptor(DependencyNodeContext context,
      DependencyNodeImpl node)
   {
      // resolve version range
      final VersionRangeResult rangeResult;
      try
      {
         rangeResult = resolveVersionRange(context, node);
         node.setVersionConstraint(rangeResult.getVersionConstraint());
      }
      catch (VersionRangeResolutionException e)
      {
         addException(node, e);
         return null;
      }

      final Artifact artifact = node.getDependency().getArtifact();

      final boolean noDescriptor = isLackingDescriptor(artifact);
      final boolean traverse = !noDescriptor
         && context.getDependencyTraverser().traverseDependency(node.getDependency());
      node.setData("traverse", Boolean.valueOf(traverse));

      ArtifactDescriptorResult descriptorResult = null;
      for (final Version version : rangeResult.getVersions())
      {
         // read artifact descriptor
         try
         {
            descriptorResult = readArtifactDescriptor(context, artifact.setVersion(version.toString()), noDescriptor);
            node.setDependency(node.getDependency().setArtifact(descriptorResult.getArtifact()));
            node.setVersion(version);
            node.setRelocations(descriptorResult.getRelocations());
            node.setAliases(descriptorResult.getAliases());
            node.setRepositories(getRemoteRepositories(rangeResult.getRepository(version), context.getRepositories()));
            node.setData("cycleNode", null);
            node.setData("descriptorResult", descriptorResult);
         }
         catch (ArtifactDescriptorException e)
         {
            addException(node, e);
            continue;
         }

         // handle cycle
         final DependencyNode cycleNode = find(context.getParentNodes(), node.getArtifact());
         if (cycleNode != null)
         {
            node.setRepositories(cycleNode.getRepositories());
            node.setChildren(cycleNode.getChildren());
            node.setData("cycleNode", cycleNode);
            continue;
         }
      }

      return descriptorResult;
   }

   private List<RemoteRepository> getRemoteRepositories(ArtifactRepository repository,
      List<RemoteRepository> repositories)
   {
      if (repository instanceof RemoteRepository)
      {
         return Collections.singletonList((RemoteRepository) repository);
      }
      else if (repository != null)
      {
         return Collections.emptyList();
      }
      return repositories;
   }

   private static void addException(final DependencyNodeImpl node, Exception e)
   {
      @SuppressWarnings("unchecked")
      Collection<Exception> exceptions = (Collection<Exception>) node.getData().get("exceptions");
      if (exceptions == null)
      {
         exceptions = new ArrayList<Exception>(1);
         node.setData("exceptions", exceptions);
      }
      exceptions.add(e);
   }

   public static DependencyNode find(List<DependencyNode> collection, Artifact artifact)
   {
      for (int i = collection.size() - 1; i >= 0; i--)
      {
         DependencyNode node = collection.get(i);

         Dependency dependency = node.getDependency();
         if (dependency == null)
         {
            break;
         }

         Artifact a = dependency.getArtifact();
         if (!a.getArtifactId().equals(artifact.getArtifactId()))
         {
            continue;
         }
         if (!a.getGroupId().equals(artifact.getGroupId()))
         {
            continue;
         }
         if (!a.getExtension().equals(artifact.getExtension()))
         {
            continue;
         }
         if (!a.getClassifier().equals(artifact.getClassifier()))
         {
            continue;
         }
         /*
          * NOTE: While a:1 and a:2 are technically different artifacts, we want to consider the path a:2 -> b:2 ->
          * a:1 a cycle in the current context. The artifacts themselves might not form a cycle but their producing
          * projects surely do. Furthermore, conflict resolution will always have to consider a:1 a loser (otherwise
          * its ancestor a:2 would get pruned and so would a:1) so there is no point in building the sub graph of
          * a:1.
          */

         return node;
      }

      return null;
   }

   private ArtifactDescriptorResult readArtifactDescriptor(final DependencyNodeContext context, Artifact artifact,
      boolean noDescriptor) throws ArtifactDescriptorException
   {
      return readArtifactDescriptor(noDescriptor, context.getSession(), context.getRequestContext(),
         context.getRequestTrace(), context.getRepositories(), artifact);
   }

   private boolean isLackingDescriptor(Artifact artifact)
   {
      return artifact.getProperty(ArtifactProperties.LOCAL_PATH, null) != null;
   }

   private VersionRangeResult resolveVersionRange(final DependencyNodeContext context, final DependencyNodeImpl node)
      throws VersionRangeResolutionException
   {
      // TODO cache
      return resolveVersionRange(context.getSession(), context.getRequestContext(), context.getRequestTrace(),
         context.getRepositories(), node.getDependency());
   }

   private ArtifactDescriptorResult readArtifactDescriptor(boolean noDescriptor, RepositorySystemSession session,
      String requestContext, RequestTrace trace, List<RemoteRepository> repositories, Artifact artifact)
      throws ArtifactDescriptorException
   {
      ArtifactDescriptorResult descriptorResult;
      ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
      descriptorRequest.setArtifact(artifact);
      descriptorRequest.setRepositories(repositories);
      descriptorRequest.setRequestContext(requestContext);
      descriptorRequest.setTrace(trace);

      if (noDescriptor)
      {
         descriptorResult = new ArtifactDescriptorResult(descriptorRequest);
      }
      else
      {
         // TODO cache
         // TODO externalize

         descriptorResult = descriptorReader.readArtifactDescriptor(session, descriptorRequest);

      }
      return descriptorResult;
   }

   private VersionRangeResult resolveVersionRange(RepositorySystemSession session, String requestContext,
      RequestTrace trace, List<RemoteRepository> repositories, Dependency dependency)
      throws VersionRangeResolutionException
   {
      VersionRangeResult rangeResult;
      VersionRangeRequest rangeRequest = new VersionRangeRequest();
      rangeRequest.setArtifact(dependency.getArtifact());
      rangeRequest.setRepositories(repositories);
      rangeRequest.setRequestContext(requestContext);
      rangeRequest.setTrace(trace);

      rangeResult = versionRangeResolver.resolveVersionRange(session, rangeRequest);

      if (rangeResult.getVersions().isEmpty())
      {
         throw new VersionRangeResolutionException(rangeResult, "No versions available for " + dependency.getArtifact()
            + " within specified range");
      }
      return rangeResult;
   }


   private static void applyDependencyManagement(DependencyNodeImpl node, DependencyManagement dependencyManagement,
      boolean savePremanagedState, boolean disableVersionManagement)
   {
      Dependency dependency = node.getDependency();
      int managedBits = node.getManagedBits();

      final String managedVersion = dependencyManagement.getVersion();
      if (managedVersion != null && !disableVersionManagement)
      {
         final Artifact artifact = dependency.getArtifact();
         if (savePremanagedState)
         {
            node.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_VERSION, artifact.getVersion());
         }
         dependency = dependency.setArtifact(artifact.setVersion(managedVersion));
         managedBits |= DependencyNode.MANAGED_VERSION;
      }

      if (dependencyManagement.getProperties() != null)
      {
         Artifact artifact = dependency.getArtifact();
         dependency = dependency.setArtifact(artifact.setProperties(dependencyManagement.getProperties()));
         managedBits |= DependencyNode.MANAGED_PROPERTIES;
      }

      if (dependencyManagement.getScope() != null)
      {
         if (savePremanagedState)
         {
            node.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_SCOPE, dependency.getScope());
         }
         dependency = dependency.setScope(dependencyManagement.getScope());
         managedBits |= DependencyNode.MANAGED_SCOPE;
      }

      if (dependencyManagement.getOptional() != null)
      {
         if (savePremanagedState)
         {
            node.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_OPTIONAL, dependency.isOptional());
         }
         dependency = dependency.setOptional(dependencyManagement.getOptional());
         managedBits |= DependencyNode.MANAGED_OPTIONAL;
      }

      if (dependencyManagement.getExclusions() != null)
      {
         dependency = dependency.setExclusions(dependencyManagement.getExclusions());
         managedBits |= DependencyNode.MANAGED_EXCLUSIONS;
      }

      node.setDependency(dependency);
      node.setManagedBits(managedBits);
   }
}
