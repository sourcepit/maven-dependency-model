/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;


import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;

public abstract class AbstractDepthBasedDependencySelector implements DependencySelector
{
   public static final int REQUIRE = -1;
   public static final int IGNORE = 0;

   protected final int requireDepth;
   protected final int currentDepth;

   protected AbstractDepthBasedDependencySelector(int requireDepth, int currentDepth)
   {
      this.requireDepth = requireDepth;
      this.currentDepth = currentDepth;
   }

   @Override
   public boolean selectDependency(Dependency dependency)
   {
      switch (requireDepth)
      {
         case REQUIRE :
            return true;
         case IGNORE :
            return false;
         default :
            return currentDepth < requireDepth || select(dependency);
      }
   }

   @Override
   public DependencySelector deriveChildSelector(DependencyCollectionContext context)
   {
      switch (requireDepth)
      {
         case REQUIRE :
         case IGNORE :
            return this;
         default :
            return deriveChildDependencySelector(context);
      }
   }

   protected abstract DependencySelector deriveChildDependencySelector(DependencyCollectionContext context);

   protected abstract boolean select(Dependency dependency);
}