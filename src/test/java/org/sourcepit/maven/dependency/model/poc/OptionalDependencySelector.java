/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
