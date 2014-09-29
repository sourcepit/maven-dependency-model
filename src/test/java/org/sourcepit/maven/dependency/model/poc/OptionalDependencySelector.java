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

public class OptionalDependencySelector extends AbstractDepthBasedDependencySelector
{
   public OptionalDependencySelector(int requireDepth)
   {
      this(requireDepth, 0);
   }

   protected OptionalDependencySelector(int requireDepth, int currentDepth)
   {
      super(requireDepth, currentDepth);
   }

   @Override
   protected boolean select(Dependency dependency)
   {
      return !dependency.isOptional();
   }

   @Override
   protected DependencySelector deriveChildDependencySelector(DependencyCollectionContext context)
   {
      return new OptionalDependencySelector(requireDepth, currentDepth + 1);
   }
}
