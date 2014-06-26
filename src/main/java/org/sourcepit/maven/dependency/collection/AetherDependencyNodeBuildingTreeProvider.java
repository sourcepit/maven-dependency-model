/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.Collections;
import java.util.List;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

public class AetherDependencyNodeBuildingTreeProvider implements TreeProvider<DependencyNodeRequest>
{
   private final TreeProvider<DependencyNodeRequest> target;

   private final boolean savePremanagedState;

   public AetherDependencyNodeBuildingTreeProvider(TreeProvider<DependencyNodeRequest> target,
      boolean savePremanagedState)
   {
      this.target = target;
      this.savePremanagedState = savePremanagedState;

   }

   @Override
   public List<DependencyNodeRequest> getRoots(List<DependencyNodeRequest> requests)
   {
      final List<DependencyNodeRequest> children = target.getRoots(requests);
      build(children);
      return children;
   }

   private void build(List<DependencyNodeRequest> requests)
   {
      for (DependencyNodeRequest request : requests)
      {
         final DependencyResolutionNode node = request.getNode();
         try
         {
            if (node.getConflictNode() == null)
            {
               setDependencyNode(node, buildNode(node));
            }
         }
         catch (VersionRangeResolutionException e)
         {
            handleException(e);
         }
         catch (ArtifactDescriptorException e)
         {
            handleException(e);
         }
      }
   }

   @Override
   public List<DependencyNodeRequest> getChildren(DependencyNodeRequest request)
   {
      final List<DependencyNodeRequest> children = target.getChildren(request);
      build(children);
      return children;
   }

   static void setDependencyNode(DependencyResolutionNode resolutionNode, DependencyNodeImpl dependencyNode)
   {
      resolutionNode.getData().put(DependencyNode.class, dependencyNode);
   }

   private DependencyNodeImpl buildNode(DependencyResolutionNode resolutionNode)
      throws VersionRangeResolutionException, ArtifactDescriptorException
   {
      final VersionRangeResult rangeResult = resolutionNode.getVersionRangeResult();

      final ManagedDependency managedDependency = resolutionNode.getManagedDependency();

      if (rangeResult.getVersions().isEmpty())
      {
         throw new VersionRangeResolutionException(rangeResult, "No versions available for "
            + managedDependency.getDependency().getArtifact() + " within specified range");
      }

      Version version = resolutionNode.getResolvedVersion();
      final ArtifactDescriptorResult descriptorResult = resolutionNode.getVersionToArtifactDescriptorResultMap().get(
         version);
      if (descriptorResult.getExceptions().isEmpty())
      {
         final Dependency collectedDependency = managedDependency.getDependency().setArtifact(
            descriptorResult.getArtifact());

         final DependencyNodeImpl node = new DependencyNodeImpl();
         node.setAliases(descriptorResult.getAliases());
         node.setDependency(collectedDependency);
         node.setManagedBits(managedDependency.getManagedBits());
         node.setRelocations(descriptorResult.getRelocations());
         node.setRepositories(getRemoteRepositories(rangeResult.getRepository(version),
            resolutionNode.getRepositories()));
         node.setRequestContext(resolutionNode.getRequestContext());
         node.setVersion(version);
         node.setVersionConstraint(rangeResult.getVersionConstraint());

         final DependencyResolutionNode cyclicParent = resolutionNode.getCyclicParent();
         if (cyclicParent != null)
         {
            final DependencyNode cyclicNode = getDependencyNode(cyclicParent);
            node.setRepositories(cyclicNode.getRepositories());
            node.setChildren(cyclicNode.getChildren());
            node.setData("cycleNode", cyclicNode);
         }

         final DependencyResolutionNode parent = resolutionNode.getParent();
         if (parent != null)
         {
            getDependencyNode(parent).getChildren().add(node);
         }

         return node;
      }
      else
      {
         throw new ArtifactDescriptorException(descriptorResult);
      }
   }

   static DependencyNode getDependencyNode(DependencyResolutionNode resolutionNode)
   {
      return (DependencyNode) resolutionNode.getData().get(DependencyNode.class);
   }

   protected void handleException(Exception e)
   {
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
}
