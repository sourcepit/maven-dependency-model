/*
 * Copyright 2014 Bernd Vogt and others.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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