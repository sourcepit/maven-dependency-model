/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import java.util.List;

import org.sonatype.aether.graph.DependencyNode;

public class NearestDependencyNodeChooser implements DependencyNodeChooser
{
   @Override
   public DependencyNode choose(List<DependencyNode> nodes)
   {
      DependencyNode chosen = null;
      int chosenDepth = Integer.MAX_VALUE;
      for (DependencyNode node : nodes)
      {
         final int nodeDepth = DependencyNode2Adapter.get(node).getMinimalDepth();
         if (chosenDepth > nodeDepth)
         {
            chosen = node;
            chosenDepth = nodeDepth;
         }
      }
      return chosen;
   }

}
