/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import org.eclipse.aether.graph.DependencyNode;

public class DependencyNodeRequest
{
   private DependencyResolutionRequest dependencyResolutionRequest;

   private DependencyResolutionResult dependencyResolutionResult;

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