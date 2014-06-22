/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.Set;

import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.version.Version;
import org.sourcepit.common.maven.model.VersionConflictKey;

public class DependencyResolutionNode
{
   private DependencyResolutionNode parent;

   private DependencyResolutionRequest dependencyResolutionRequest;

   private DependencyResolutionResult dependencyResolutionResult;

   private Set<VersionConflictKey> conflictKeys;

   private Version resolvedVersion;

   private DependencyResolutionNode cyclicParent;

   public DependencyResolutionNode(DependencyResolutionNode parent)
   {
      this.parent = parent; 
   }

   public DependencyResolutionNode getParent()
   {
      return parent;
   }

   public void setParent(DependencyResolutionNode parent)
   {
      this.parent = parent;
   }

   public void setDependencyResolutionRequest(DependencyResolutionRequest dependencyResolutionRequest)
   {
      this.dependencyResolutionRequest = dependencyResolutionRequest;
   }

   public DependencyResolutionRequest getDependencyResolutionRequest()
   {
      return dependencyResolutionRequest;
   }

   public void setDependencyResolutionResult(DependencyResolutionResult dependencyResolutionResult)
   {
      this.dependencyResolutionResult = dependencyResolutionResult;
   }

   public DependencyResolutionResult getDependencyResolutionResult()
   {
      return dependencyResolutionResult;
   }

   public void setConflictKeys(Set<VersionConflictKey> conflictKeys)
   {
      this.conflictKeys = conflictKeys;
   }

   public Set<VersionConflictKey> getConflictKeys()
   {
      return conflictKeys;
   }

   public void setResolvedVersion(Version resolvedVersion)
   {
      this.resolvedVersion = resolvedVersion;
   }

   public Version getResolvedVersion()
   {
      return resolvedVersion;
   }

   public void setCyclicParent(DependencyResolutionNode cyclicParent)
   {
      this.cyclicParent = cyclicParent;
   }

   public DependencyResolutionNode getCyclicParent()
   {
      return cyclicParent;
   }

   private DependencyNodeContext context;

   private DependencyNode dependencyNode;

   public void setContext(DependencyNodeContext context)
   {
      this.context = context;
   }

   public DependencyNodeContext getContext()
   {
      return context;
   }

   public void setDependencyNode(DependencyNode dependencyNode)
   {
      this.dependencyNode = dependencyNode;
   }

   public DependencyNode getDependencyNode()
   {
      return dependencyNode;
   }
}