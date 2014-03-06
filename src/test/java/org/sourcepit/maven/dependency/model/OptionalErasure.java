/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;

public class OptionalErasure implements DependencySelector
{
   @Override
   public boolean selectDependency(Dependency dependency)
   {
      return !dependency.isOptional();
   }

   @Override
   public DependencySelector deriveChildSelector(DependencyCollectionContext context)
   {
      return this;
   }
}
