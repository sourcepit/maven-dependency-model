/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.DependencyNode;

public class DependencyNode2AdapterTransformer implements DependencyGraphTransformer
{
   private final boolean reset;

   public DependencyNode2AdapterTransformer(boolean reset)
   {
      this.reset = reset;
   }

   @Override
   public DependencyNode transformGraph(DependencyNode node, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      DependencyNode2Adapter.adapt(node, reset);
      return node;
   }

}
