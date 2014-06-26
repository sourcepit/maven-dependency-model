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
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

public class DependencyResolvingTreeProvider implements TreeProvider<DependencyNodeRequest>
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
   public List<DependencyNodeRequest> getRoots(List<DependencyNodeRequest> requests)
   {
      return resolve(requests);
   }

   @Override
   public List<DependencyNodeRequest> getChildren(DependencyNodeRequest request)
   {
      final DependencyNodeManager nodeManager = request.getNodeManager();
      final DependencyResolutionNode node = request.getNode();

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
      final boolean traverse = !noDescriptor && nodeManager.traverseDependency(managedDependency);

      if (!traverse)
      {
         return Collections.emptyList();
      }

      final Artifact artifact = descriptorResult.getArtifact();

      // traverse children (if traverse dependencies)
      final List<Dependency> children = nodeManager.filterDependencies(artifact, descriptorResult.getDependencies());

      if (children.isEmpty())
      {
         return Collections.emptyList();
      }

      final Dependency resolvedDependency = managedDependency.setArtifact(descriptorResult.getArtifact());

      final RepositorySystemSession session = request.getSession();

      final DependencyNodeManager childManager = nodeManager.deriveChildManager(session, resolvedDependency,
         descriptorResult.getManagedDependencies());

      final List<RemoteRepository> childRepositories = aggregateRepositories(session, node.getRepositories(),
         descriptorResult.getRepositories());

      final List<DependencyNodeRequest> childRequests = new ArrayList<DependencyNodeRequest>(children.size());
      for (Dependency child : children)
      {
         // if selectDependency
         if (!childManager.selectDependency(child))
         {
            continue;
         }

         final RequestTrace trace = request.getTrace();
         final DependencyResolutionNode childNode = new DependencyResolutionNode(node, childRepositories, child);
         childRequests.add(new DependencyNodeRequest(session, trace, childManager, childNode));
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

   private List<DependencyNodeRequest> resolve(List<DependencyNodeRequest> requests)
   {
      for (DependencyNodeRequest request : requests)
      {
         resolve(request);
      }
      return requests;
   }

   private void resolve(DependencyNodeRequest request)
   {
      final DependencyResolutionRequest resolutionRequest = newDependencyResolutionRequest(request);
      final DependencyResolutionResult resolutionResult = dependencyResolver.resolveDependency(resolutionRequest);

      final DependencyResolutionNode node = request.getNode();
      node.setManagedDependency(resolutionResult.getManagedDependency());
      node.setVersionRangeResult(resolutionResult.getVersionRangeResult());
      node.setVersionToArtifactDescriptorResultMap(resolutionResult.getVersionToArtifactDescriptorResultMap());

      final VersionRangeResult versionRangeResult = resolutionResult.getVersionRangeResult();
      if (versionRangeResult != null)
      {
         node.setResolvedVersion(versionChooser.chooseVersion(versionRangeResult));
      }
   }

   private static DependencyResolutionRequest newDependencyResolutionRequest(DependencyNodeRequest request)
   {
      final DependencyResolutionRequest resolutionRequest = new DependencyResolutionRequest();
      resolutionRequest.setSession(request.getSession());
      resolutionRequest.setRequestTrace(request.getTrace());

      final DependencyNodeManager nodeManager = request.getNodeManager();
      resolutionRequest.setDependencyManager(nodeManager);
      resolutionRequest.setDependencySelector(nodeManager);

      final DependencyResolutionNode node = request.getNode();
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
