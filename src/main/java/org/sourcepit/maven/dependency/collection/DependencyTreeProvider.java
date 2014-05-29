/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

public class DependencyTreeProvider implements TreeProvider<DependencyNodeRequest>
{
   private DescriptorResolver descriptorResolver;

   public DependencyTreeProvider(DescriptorResolver descriptorResolver)
   {
      this.descriptorResolver = descriptorResolver;
   }

   @Override
   public List<DependencyNodeRequest> visitChildren(DependencyNodeRequest parent, int depth,
      List<DependencyNodeRequest> children)
   {
      final List<DependencyNodeRequest> bar = new ArrayList<DependencyNodeRequest>(children.size());
      for (DependencyNodeRequest request : children)
      {
         final DependencyNodeContext context = request.getContext();

         final DescriptorResolutionRequest descriptorRequest = new DescriptorResolutionRequest();
         descriptorRequest.setDependency(request.getDependency());
         descriptorRequest.setSession(context.getSession());
         descriptorRequest.setDependencyManager(context.getDependencyManager());
         descriptorRequest.setDependencySelector(context.getDependencySelector());
         descriptorRequest.setRequestContext(context.getRequestContext());
         descriptorRequest.setRequestTrace(context.getRequestTrace());
         descriptorRequest.setRepositories(context.getRepositories());

         final DescriptorResolutionResult result = descriptorResolver.resolveDescriptors(descriptorRequest);

         final List<DependencyNodeImpl> foo = buildNodes(result, context);

         if (foo.size() == 0)
         {
            bar.add(request);
         }
         else if (foo.size() == 1)
         {
            request.setDependencyNode(foo.get(0));
            bar.add(request);
         }
         else
         {
            for (DependencyNodeImpl dependencyNodeImpl : foo)
            {
               DependencyNodeRequest r = new DependencyNodeRequest();
               r.setContext(request.getContext());
               r.setDependency(request.getDependency());
               r.setDependencyNode(dependencyNodeImpl);
               bar.add(r);
            }
         }
      }
      return bar;
   }

   @Override
   public void leaveChildren(DependencyNodeRequest parent, int depth, List<DependencyNodeRequest> children)
   {
   }

   @Override
   public List<DependencyNodeRequest> getChildren(DependencyNodeRequest request)
   {
      final DependencyNodeContext context = request.getContext();

      final DependencyNodeImpl node = (DependencyNodeImpl) request.getDependencyNode();
      if (node == null)
      {
         return Collections.emptyList();
      }

      final LinkedList<DependencyNode> parentNodes = context.getParentNodes();

      final DependencyNode cyclicNode = find(parentNodes, node.getArtifact());
      if (cyclicNode != null)
      {
         node.setRepositories(cyclicNode.getRepositories());
         node.setChildren(cyclicNode.getChildren());
         node.setData("cycleNode", cyclicNode);
         return Collections.emptyList();
      }

      if (!parentNodes.isEmpty())
      {
         final DependencyNode parentNode = parentNodes.getLast();
         parentNode.getChildren().add(node);
      }

      final Dependency managedDependency = (Dependency) node.getData().get("managedDependency");
      final boolean noDescriptor = isLackingDescriptor(managedDependency.getArtifact());
      final boolean traverse = !noDescriptor && context.getDependencyTraverser().traverseDependency(managedDependency);

      if (!traverse)
      {
         return Collections.emptyList();
      }

      final ArtifactDescriptorResult descriptorResult = (ArtifactDescriptorResult) node.getData().get(
         "descriptorResult");

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

   private List<DependencyNodeImpl> buildNodes(final DescriptorResolutionResult result,
      final DependencyNodeContext context)
   {
      List<DependencyNodeImpl> nodes = new ArrayList<DependencyNodeImpl>(result.getArtifactDescriptorResults().size());

      final VersionRangeResult rangeResult = result.getVersionRangeResult();

      final ManagedDependency managedDependency = result.getManagedDependency();

      if (rangeResult.getVersions().isEmpty())
      {
         addException(null, new VersionRangeResolutionException(rangeResult, "No versions available for "
            + managedDependency.getDependency().getArtifact() + " within specified range"));
      }
      else
      {
         for (Version version : rangeResult.getVersions())
         {
            final ArtifactDescriptorResult descriptorResult = result.getArtifactDescriptorResults().get(version);
            if (descriptorResult.getExceptions().isEmpty())
            {
               final Dependency collectedDependency = managedDependency.getDependency().setArtifact(
                  descriptorResult.getArtifact());

               final DependencyNodeImpl node = new DependencyNodeImpl();
               node.setAliases(descriptorResult.getAliases());
               node.setDependency(collectedDependency);
               node.setManagedBits(managedDependency.getManagedBits());
               node.setRelocations(descriptorResult.getRelocations());
               node
                  .setRepositories(getRemoteRepositories(rangeResult.getRepository(version), context.getRepositories()));
               node.setRequestContext(context.getRequestContext());
               node.setVersion(version);
               node.setVersionConstraint(rangeResult.getVersionConstraint());

               node.setData("managedDependency", managedDependency.getDependency());
               node.setData("descriptorResult", descriptorResult);

               nodes.add(node);
            }
            else
            {
               addException(null, new ArtifactDescriptorException(descriptorResult));
            }
         }
      }

      return nodes;
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

   protected void addException(final DependencyNodeImpl node, Exception e)
   {
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

   private boolean isLackingDescriptor(Artifact artifact)
   {
      return artifact.getProperty(ArtifactProperties.LOCAL_PATH, null) != null;
   }


}
