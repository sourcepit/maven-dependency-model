/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import static java.util.Collections.addAll;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;

public class ScopeErasure implements DependencySelector
{
   private final Set<String> excludedScopes;

   public ScopeErasure(String... excludedScopes)
   {
      this.excludedScopes = new HashSet<String>(excludedScopes.length);
      addAll(this.excludedScopes, excludedScopes);
   }

   @Override
   public boolean selectDependency(Dependency dependency)
   {
      return !excludedScopes.contains(dependency.getScope());
   }

   @Override
   public DependencySelector deriveChildSelector(DependencyCollectionContext context)
   {
      return this;
   }

}
