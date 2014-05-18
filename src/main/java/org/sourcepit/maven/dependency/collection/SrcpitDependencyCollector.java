/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyManagement;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.ConfigUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.version.Version;

@Named("srcpit")
public class SrcpitDependencyCollector implements DependencyCollector
{
   @Inject
   private RemoteRepositoryManager remoteRepositoryManager;

   @Inject
   private ArtifactDescriptorReader descriptorReader;

   @Inject
   private VersionRangeResolver versionRangeResolver;

   @Override
   public CollectResult collectDependencies(RepositorySystemSession session, CollectRequest request)
      throws DependencyCollectionException
   {

      RequestTrace trace = RequestTrace.newChild(request.getTrace(), request);

      DependencySelector depSelector = session.getDependencySelector();
      DependencyManager depManager = session.getDependencyManager();
      DependencyTraverser depTraverser = session.getDependencyTraverser();

      List<RemoteRepository> repositories = request.getRepositories();
      List<Dependency> dependencies = request.getDependencies();
      List<Dependency> managedDependencies = request.getManagedDependencies();


      CollectResult result = new CollectResult(request);

      // TODO
      Artifact rootArtifact = null; // request.getRootArtifact();
      Dependency root = null; // request.getRoot();

      DefaultDependencyNode node = new DefaultDependencyNode(rootArtifact);

      result.setRoot(node);

      if (!dependencies.isEmpty())
      {

         final boolean premanagedState = ConfigUtils.getBoolean(session, false,
            DependencyManagerUtils.CONFIG_PROP_VERBOSE);

         NodeContext nodeCtx = new NodeContext();
         nodeCtx.nodes = new ArrayList<DependencyNode>(1);
         nodeCtx.nodes.add(node);

         DefaultDependencyCollectionContext context = new DefaultDependencyCollectionContext(session, root,
            managedDependencies);
         nodeCtx.depSelector = depSelector.deriveChildSelector(context);
         nodeCtx.depTraverser = depTraverser.deriveChildTraverser(context);
         nodeCtx.depManager = depManager.deriveChildManager(context);

         nodeCtx.repositories = repositories;

         process(premanagedState, session, request.getRequestContext(), trace, dependencies, nodeCtx);
      }


      return result;
   }

   private class NodeContext
   {
      List<DependencyNode> nodes;
      List<RemoteRepository> repositories;
      DependencySelector depSelector;
      DependencyManager depManager;
      DependencyTraverser depTraverser;
   }

   private class DependencyManagementResult
   {
      int managedBits = 0;
      String premanagedVersion = null;
      String premanagedScope = null;
      Boolean premanagedOptional = null;
   }

   private void process(boolean premanagedState, RepositorySystemSession session, String requestContext,
      RequestTrace trace, List<Dependency> dependencies, NodeContext nodeContext)
   {
      nextDependency : for (Dependency dependency : dependencies)
      {
         boolean disableVersionManagement = false;

         List<Artifact> relocations = Collections.emptyList();

         thisDependency : while (true)
         {
            // if selectDependency
            
            if (!nodeContext.depSelector.selectDependency(dependency))
            {
               continue nextDependency;
            }

            // apply dependency management
            
            DependencyManagement depMngt = nodeContext.depManager.manageDependency(dependency);

            final DependencyManagementResult depMngtResult = new DependencyManagementResult();

            if (depMngt != null)
            {
               dependency = applyDependencyManagement(dependency, disableVersionManagement, depMngt, depMngtResult);
            }
            disableVersionManagement = false;

            boolean noDescriptor = isLackingDescriptor(dependency.getArtifact());
            
            // resolve version range

            // TODO cache
            VersionRangeResult rangeResult;
            try
            {
               rangeResult = resolveVersionRange(session, requestContext, trace, nodeContext.repositories, dependency);
            }
            catch (VersionRangeResolutionException e)
            {
               addException(dependency, e);
               continue nextDependency;
            }

            List<Version> versions = rangeResult.getVersions();
            for (Version version : versions)
            {
               Artifact originalArtifact = dependency.getArtifact().setVersion(version.toString());
               Dependency d = dependency.setArtifact(originalArtifact);
               
               // read artifact descriptor

               ArtifactDescriptorResult descriptorResult;
               try
               {
                  descriptorResult = readArtifactDescriptor(session, requestContext, trace, nodeContext.repositories,
                     noDescriptor, d);
               }
               catch (ArtifactDescriptorException e)
               {
                  addException(d, e);
                  continue;
               }

               d = d.setArtifact(descriptorResult.getArtifact());

               DependencyNode node = nodeContext.nodes.get(nodeContext.nodes.size() - 1);
               
               // handle cycle

               DependencyNode cycleNode = find(nodeContext.nodes, d.getArtifact());
               if (cycleNode != null)
               {
                  DefaultDependencyNode child = newDefaultDependencyNode(premanagedState,
                     cycleNode.getRequestContext(), relocations, depMngtResult, rangeResult, version, d,
                     descriptorResult, cycleNode.getRepositories());
                  child.setChildren(cycleNode.getChildren());
                  node.getChildren().add(child);

                  continue;
               }
               
               // handle relocation

               if (!descriptorResult.getRelocations().isEmpty())
               {
                  relocations = descriptorResult.getRelocations();

                  disableVersionManagement = originalArtifact.getGroupId().equals(d.getArtifact().getGroupId())
                     && originalArtifact.getArtifactId().equals(d.getArtifact().getArtifactId());

                  dependency = d;
                  continue thisDependency;
               }
               
               // traverse children (if traverse dependencies)

               List<RemoteRepository> repos = getRemoteRepositories(rangeResult.getRepository(version),
                  nodeContext.repositories);

               DefaultDependencyNode child = newDefaultDependencyNode(premanagedState, requestContext, relocations,
                  depMngtResult, rangeResult, version, d, descriptorResult, repos);

               node.getChildren().add(child);


               boolean traverse = !noDescriptor && nodeContext.depTraverser.traverseDependency(dependency);

               boolean recurse = traverse && !descriptorResult.getDependencies().isEmpty();
               if (recurse)
               {
                  DefaultDependencyCollectionContext context = new DefaultDependencyCollectionContext(session, d,
                     descriptorResult.getManagedDependencies());

                  NodeContext ctx = new NodeContext();

                  ctx.depSelector = nodeContext.depSelector.deriveChildSelector(context);
                  ctx.depManager = nodeContext.depManager.deriveChildManager(context);
                  ctx.depTraverser = nodeContext.depTraverser.deriveChildTraverser(context);

                  List<RemoteRepository> childRepos = null;
                  if (session.isIgnoreArtifactDescriptorRepositories())
                  {
                     childRepos = nodeContext.repositories;
                  }
                  else
                  {
                     childRepos = remoteRepositoryManager.aggregateRepositories(session, nodeContext.repositories,
                        descriptorResult.getRepositories(), true);
                  }

                  ctx.repositories = childRepos;

                  // TODO cache

                  ctx.nodes = new ArrayList<DependencyNode>(nodeContext.nodes);
                  ctx.nodes.add(child);

                  process(premanagedState, session, requestContext, trace, descriptorResult.getDependencies(), ctx);
               }
            }

            break;
         }
      }
   }

   private DefaultDependencyNode newDefaultDependencyNode(boolean premanagedState, String requestContext,
      List<Artifact> relocations, final DependencyManagementResult depMngtResult, VersionRangeResult rangeResult,
      Version version, Dependency d, ArtifactDescriptorResult descriptorResult, List<RemoteRepository> repos)
   {
      DefaultDependencyNode child = new DefaultDependencyNode(d);
      child.setManagedBits(depMngtResult.managedBits);
      if (premanagedState)
      {
         child.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_VERSION, depMngtResult.premanagedVersion);
         child.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_SCOPE, depMngtResult.premanagedScope);
         child.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_OPTIONAL, depMngtResult.premanagedOptional);
      }
      child.setRelocations(relocations);
      child.setVersionConstraint(rangeResult.getVersionConstraint());
      child.setVersion(version);
      child.setAliases(descriptorResult.getAliases());
      child.setRepositories(repos);
      child.setRequestContext(requestContext);
      return child;
   }

   private static Dependency applyDependencyManagement(Dependency dependency, boolean disableVersionManagement,
      DependencyManagement depMngt, DependencyManagementResult depMngtResult)
   {
      if (depMngt.getVersion() != null && !disableVersionManagement)
      {
         Artifact artifact = dependency.getArtifact();
         depMngtResult.premanagedVersion = artifact.getVersion();
         dependency = dependency.setArtifact(artifact.setVersion(depMngt.getVersion()));
         depMngtResult.managedBits |= DependencyNode.MANAGED_VERSION;
      }
      if (depMngt.getProperties() != null)
      {
         Artifact artifact = dependency.getArtifact();
         dependency = dependency.setArtifact(artifact.setProperties(depMngt.getProperties()));
         depMngtResult.managedBits |= DependencyNode.MANAGED_PROPERTIES;
      }
      if (depMngt.getScope() != null)
      {
         depMngtResult.premanagedScope = dependency.getScope();
         dependency = dependency.setScope(depMngt.getScope());
         depMngtResult.managedBits |= DependencyNode.MANAGED_SCOPE;
      }
      if (depMngt.getOptional() != null)
      {
         depMngtResult.premanagedOptional = dependency.isOptional();
         dependency = dependency.setOptional(depMngt.getOptional());
         depMngtResult.managedBits |= DependencyNode.MANAGED_OPTIONAL;
      }
      if (depMngt.getExclusions() != null)
      {
         dependency = dependency.setExclusions(depMngt.getExclusions());
         depMngtResult.managedBits |= DependencyNode.MANAGED_EXCLUSIONS;
      }
      return dependency;
   }

   private ArtifactDescriptorResult readArtifactDescriptor(RepositorySystemSession session, String requestContext,
      RequestTrace trace, List<RemoteRepository> repositories, boolean noDescriptor, Dependency d)
      throws ArtifactDescriptorException
   {
      ArtifactDescriptorResult descriptorResult;
      ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
      descriptorRequest.setArtifact(d.getArtifact());
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

   private boolean isLackingDescriptor(Artifact artifact)
   {
      return artifact.getProperty(ArtifactProperties.LOCAL_PATH, null) != null;
   }

   private void addException(Dependency dependency, Exception e)
   {
      // TODO
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

}
