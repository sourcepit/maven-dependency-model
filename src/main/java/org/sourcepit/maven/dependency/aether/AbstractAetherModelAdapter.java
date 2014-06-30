/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.aether;

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
import org.sourcepit.maven.dependency.CustomModelAdapter;
import org.sourcepit.maven.dependency.ManagedDependency;

public abstract class AbstractAetherModelAdapter implements CustomModelAdapter<DependencyNode>
{
   private static final class AdaptResult
   {
      private DependencyNode node;
   }

   private final boolean savePremanagedState;

   public AbstractAetherModelAdapter(boolean savePremanagedState)
   {
      this.savePremanagedState = savePremanagedState;
   }

   @Override
   public DependencyNode adapt(org.sourcepit.maven.dependency.DependencyNode node)
   {
      AdaptResult result = getAdaptResult(node);
      if (result == null)
      {
         result = new AdaptResult();
         try
         {
            if (node.getConflictNode() == null)
            {
               result.node = newDependencyNode(node);
            }
         }
         catch (VersionRangeResolutionException e)
         {
            handleException(node, e);
         }
         catch (ArtifactDescriptorException e)
         {
            handleException(node, e);
         }
         setAdaptResult(node, result);
      }

      return result.node;
   }

   protected abstract void handleException(org.sourcepit.maven.dependency.DependencyNode node, Exception e);

   private DependencyNode newDependencyNode(org.sourcepit.maven.dependency.DependencyNode resolutionNode)
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
         node.setRequestContext(node.getRequestContext());
         node.setVersion(version);
         node.setVersionConstraint(rangeResult.getVersionConstraint());

         final org.sourcepit.maven.dependency.DependencyNode cyclicParent = resolutionNode.getCyclicParent();
         if (cyclicParent != null)
         {
            final DependencyNode cyclicNode = getDependencyNode(cyclicParent);
            node.setRepositories(cyclicNode.getRepositories());
            node.setChildren(cyclicNode.getChildren());
            node.setData("cycleNode", cyclicNode);
         }

         final org.sourcepit.maven.dependency.DependencyNode parent = resolutionNode.getParent();
         if (parent != null)
         {
            final DependencyNode parentNode = getDependencyNode(parent);
            if (parentNode != null)
            {
               parentNode.getChildren().add(node);
            }
         }

         return node;
      }
      else
      {
         throw new ArtifactDescriptorException(descriptorResult);
      }
   }

   private static void setAdaptResult(final org.sourcepit.maven.dependency.DependencyNode node, AdaptResult result)
   {
      node.getData().put(AdaptResult.class, result);
   }

   private static AdaptResult getAdaptResult(final org.sourcepit.maven.dependency.DependencyNode node)
   {
      return (AdaptResult) node.getData().get(AdaptResult.class);
   }

   private static DependencyNode getDependencyNode(org.sourcepit.maven.dependency.DependencyNode resolutionNode)
   {
      AdaptResult adaptResult = getAdaptResult(resolutionNode);
      return adaptResult == null ? null : adaptResult.node;
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

   public void assign(org.sourcepit.maven.dependency.DependencyNode node, DependencyNode aetherNode)
   {
      final AdaptResult result = new AdaptResult();
      result.node = aetherNode;
      setAdaptResult(node, result);
   }

}
