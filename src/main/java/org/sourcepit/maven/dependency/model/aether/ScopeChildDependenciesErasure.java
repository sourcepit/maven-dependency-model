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

package org.sourcepit.maven.dependency.model.aether;

import java.util.Collection;

import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.graph.selector.StaticDependencySelector;
import org.sourcepit.common.constraints.NotNull;

public class ScopeChildDependenciesErasure implements DependencySelector {
   private final Collection<String> scopes;

   public ScopeChildDependenciesErasure(@NotNull Collection<String> scopes) {
      this.scopes = scopes;
   }

   @Override
   public boolean selectDependency(Dependency dependency) {
      return true;
   }

   @Override
   public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
      if (context.getDependency() == null || selectChildDependencies(context.getDependency())) {
         return this;
      }
      return new StaticDependencySelector(false);
   }

   private boolean selectChildDependencies(Dependency dependency) {
      return !scopes.contains(dependency.getScope());
   }
}
