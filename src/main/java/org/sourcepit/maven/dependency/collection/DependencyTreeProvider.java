/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.version.Version;

public class DependencyTreeProvider implements TreeProvider<DependencyResolutionNode>
{
   private DependencyResolver dependencyResolver;

   private ConflictSolver conflictSolver;

   public DependencyTreeProvider(DependencyResolver dependencyResolver, ConflictSolver conflictSolver)
   {
      this.dependencyResolver = dependencyResolver;
      this.conflictSolver = conflictSolver;
   }

   @Override
   public List<DependencyResolutionNode> visitChildren(DependencyResolutionNode parent, int depth,
      List<DependencyResolutionNode> children)
   {
      for (DependencyResolutionNode node : children)
      {
         resolve(node);
         node.setCyclicParent(conflictSolver.detectCyclicParent(node));
      }

      conflictSolver.solveConflicts(parent, children);

      return children;
   }

   private void resolve(DependencyResolutionNode node)
   {
      final DependencyNodeContext context = node.getContext();
      final Dependency dependency = node.getDependency();

      final DependencyResolutionRequest resolutionRequest = newDependencyResolutionRequest(context, dependency);
      final DependencyResolutionResult resolutionResult = dependencyResolver.resolveDependency(resolutionRequest);

      node.setManagedDependency(resolutionResult.getManagedDependency());
      node.setVersionRangeResult(resolutionResult.getVersionRangeResult());
      node.setVersionToArtifactDescriptorResultMap(resolutionResult.getVersionToArtifactDescriptorResultMap());

      node.setResolvedVersion(conflictSolver.determineResolvedVersion(node));
   }

   @Override
   public void leaveChildren(DependencyResolutionNode parent, int depth, List<DependencyResolutionNode> children)
   {
   }

   @Override
   public List<DependencyResolutionNode> getChildren(DependencyResolutionNode request)
   {
      if (request.getConflictNode() != null)
      {
         return Collections.emptyList();
      }

      if (request.getCyclicParent() != null)
      {
         return Collections.emptyList();
      }
      
      final DependencyNodeContext context = request.getContext();

      final Version resolvedVersion = request.getResolvedVersion();
      if (resolvedVersion == null)
      {
         return Collections.emptyList();
      }

      final ArtifactDescriptorResult descriptorResult = request.getVersionToArtifactDescriptorResultMap().get(
         resolvedVersion);

      if (!descriptorResult.getExceptions().isEmpty())
      {
         return Collections.emptyList();
      }

      final Dependency managedDependency = request.getManagedDependency().getDependency();
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
         descriptorResult.getManagedDependencies(), descriptorResult.getRepositories());

      final DependencySelector dependencySelector = childContext.getDependencySelector();

      final List<DependencyResolutionNode> childRequests = new ArrayList<DependencyResolutionNode>(children.size());
      for (Dependency child : children)
      {
         // if selectDependency
         if (!dependencySelector.selectDependency(child))
         {
            continue;
         }

         final DependencyResolutionNode childRequest = new DependencyResolutionNode(childContext, request, child);
         childRequests.add(childRequest);
      }

      return childRequests;
   }

   private boolean isLackingDescriptor(Artifact artifact)
   {
      return artifact.getProperty(ArtifactProperties.LOCAL_PATH, null) != null;
   }

   static DependencyResolutionRequest newDependencyResolutionRequest(final DependencyNodeContext context,
      final Dependency dependency)
   {
      final DependencyResolutionRequest resolutionRequest = new DependencyResolutionRequest();
      resolutionRequest.setDependency(dependency);
      resolutionRequest.setSession(context.getSession());
      resolutionRequest.setDependencyManager(context.getDependencyManager());
      resolutionRequest.setDependencySelector(context.getDependencySelector());
      resolutionRequest.setRequestContext(context.getRequestContext());
      resolutionRequest.setRequestTrace(context.getRequestTrace());
      resolutionRequest.setRepositories(context.getRepositories());
      return resolutionRequest;
   }


}
