/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.DependencyNode;

public class HideReplacedNodes implements DependencyGraphTransformer
{
   @Override
   public DependencyNode transformGraph(DependencyNode graph, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      graph.accept(new AbstractDependencyVisitor(false)
      {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            final int depth = path.size();
            boolean visible = DependencyNode2Adapter.get(node).isVisible();
            if (visible && depth > 1)
            {
               DependencyNode2 parentAdapter = DependencyNode2Adapter.get(parent);
               visible = parentAdapter.isVisible() && parentAdapter.getReplacement() == null;
            }
            DependencyNode2Adapter.get(node).setVisible(visible);
            return true;
         }
      });
      return graph;
   }

}
