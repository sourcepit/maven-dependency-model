/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

public class DependencyResolvingTreeProvider implements TreeProvider<DependencyResolutionNode>
{
   private final RemoteRepositoryManager remoteRepositoryManager;

   private final DependencyResolver dependencyResolver;

   private final VersionChooser versionChooser;

   public DependencyResolvingTreeProvider(RemoteRepositoryManager remoteRepositoryManager,
      DependencyResolver dependencyResolver, VersionChooser versionChooser)
   {
      this.remoteRepositoryManager = remoteRepositoryManager;
      this.dependencyResolver = dependencyResolver;
      this.versionChooser = versionChooser;
   }

   @Override
   public List<DependencyResolutionNode> getRoots(List<DependencyResolutionNode> roots)
   {
      return resolve(roots);
   }

   @Override
   public List<DependencyResolutionNode> getChildren(DependencyResolutionNode node)
   {
      final DependencyNodeContext context = node.getContext();

      final Version resolvedVersion = node.getResolvedVersion();
      if (resolvedVersion == null)
      {
         return Collections.emptyList();
      }

      final ArtifactDescriptorResult descriptorResult = node.getVersionToArtifactDescriptorResultMap().get(
         resolvedVersion);

      if (!descriptorResult.getExceptions().isEmpty())
      {
         return Collections.emptyList();
      }

      final Dependency managedDependency = node.getManagedDependency().getDependency();
      final boolean noDescriptor = isLackingDescriptor(managedDependency.getArtifact());
      final boolean traverse = !noDescriptor && context.getDependencyTraverser().traverseDependency(managedDependency);

      if (!traverse)
      {
         return Collections.emptyList();
      }

      final Artifact artifact = descriptorResult.getArtifact();

      // traverse children (if traverse dependencies)
      final List<Dependency> children = context.getDependenciesFilter().filterDependencies(artifact,
         descriptorResult.getDependencies());

      if (children.isEmpty())
      {
         return Collections.emptyList();
      }

      final Dependency resolvedDependency = managedDependency.setArtifact(descriptorResult.getArtifact());

      final DependencyNodeContext childContext = context.deriveChildContext(resolvedDependency,
         descriptorResult.getManagedDependencies());

      final List<RemoteRepository> childRepositories = aggregateRepositories(childContext.getSession(),
         node.getRepositories(), descriptorResult.getRepositories());

      final DependencySelector dependencySelector = childContext.getDependencySelector();

      final List<DependencyResolutionNode> childRequests = new ArrayList<DependencyResolutionNode>(children.size());
      for (Dependency child : children)
      {
         // if selectDependency
         if (!dependencySelector.selectDependency(child))
         {
            continue;
         }

         childRequests.add(new DependencyResolutionNode(childContext, node, childRepositories, child));
      }

      return resolve(childRequests);
   }

   private List<RemoteRepository> aggregateRepositories(RepositorySystemSession session,
      List<RemoteRepository> repositories, List<RemoteRepository> artifactDescriptorRepositories)
   {
      final List<RemoteRepository> childRepos;
      if (session.isIgnoreArtifactDescriptorRepositories())
      {
         childRepos = repositories;
      }
      else
      {
         childRepos = remoteRepositoryManager.aggregateRepositories(session, repositories,
            artifactDescriptorRepositories, true);
      }
      return childRepos;
   }

   private List<DependencyResolutionNode> resolve(List<DependencyResolutionNode> nodes)
   {
      for (DependencyResolutionNode node : nodes)
      {
         resolve(node);
      }
      return nodes;
   }

   private void resolve(DependencyResolutionNode node)
   {
      final DependencyNodeContext context = node.getContext();

      final DependencyResolutionRequest resolutionRequest = newDependencyResolutionRequest(context, node);
      final DependencyResolutionResult resolutionResult = dependencyResolver.resolveDependency(resolutionRequest);

      node.setManagedDependency(resolutionResult.getManagedDependency());
      node.setVersionRangeResult(resolutionResult.getVersionRangeResult());
      node.setVersionToArtifactDescriptorResultMap(resolutionResult.getVersionToArtifactDescriptorResultMap());

      final VersionRangeResult versionRangeResult = resolutionResult.getVersionRangeResult();
      if (versionRangeResult != null)
      {
         node.setResolvedVersion(versionChooser.chooseVersion(versionRangeResult));
      }
   }

   static DependencyResolutionRequest newDependencyResolutionRequest(final DependencyNodeContext context,
      DependencyResolutionNode node)
   {
      final DependencyResolutionRequest resolutionRequest = new DependencyResolutionRequest();
      resolutionRequest.setSession(context.getSession());
      resolutionRequest.setDependencyManager(context.getDependencyManager());
      resolutionRequest.setDependencySelector(context.getDependencySelector());
      resolutionRequest.setRequestTrace(context.getRequestTrace());
      resolutionRequest.setRequestContext(node.getRequestContext());
      resolutionRequest.setRepositories(node.getRepositories());
      resolutionRequest.setDependency(node.getDependency());
      return resolutionRequest;
   }

   private boolean isLackingDescriptor(Artifact artifact)
   {
      return artifact.getProperty(ArtifactProperties.LOCAL_PATH, null) != null;
   }

}
