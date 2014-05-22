/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;

public class DependencyNodeRequest
{
   private DependencyNodeContext context;

   private Dependency dependency;

   private DependencyNode dependencyNode;

   public void setContext(DependencyNodeContext context)
   {
      this.context = context;
   }

   public DependencyNodeContext getContext()
   {
      return context;
   }

   public void setDependency(Dependency dependency)
   {
      this.dependency = dependency;
   }

   public Dependency getDependency()
   {
      return dependency;
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