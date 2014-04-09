/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import java.util.Collection;

import org.sourcepit.common.constraints.NotNull;

import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.graph.selector.StaticDependencySelector;

public class ScopeChildDependenciesErasure implements DependencySelector
{
   private final Collection<String> scopes;

   public ScopeChildDependenciesErasure(@NotNull Collection<String> scopes)
   {
      this.scopes = scopes;
   }

   @Override
   public boolean selectDependency(Dependency dependency)
   {
      return true;
   }

   @Override
   public DependencySelector deriveChildSelector(DependencyCollectionContext context)
   {
      if (context.getDependency() == null || selectChildDependencies(context.getDependency()))
      {
         return this;
      }
      return new StaticDependencySelector(false);
   }

   private boolean selectChildDependencies(Dependency dependency)
   {
      return !scopes.contains(dependency.getScope());
   }
}
