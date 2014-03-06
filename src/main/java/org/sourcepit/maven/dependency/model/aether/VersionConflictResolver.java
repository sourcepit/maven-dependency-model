/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import java.util.List;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.DependencyNode;

public class VersionConflictResolver implements DependencyGraphTransformer
{
   private DependencyNodeChooser nodeChooser;

   public VersionConflictResolver(DependencyNodeChooser nodeChooser)
   {
      this.nodeChooser = nodeChooser;
   }

   @Override
   public DependencyNode transformGraph(DependencyNode node, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      final DependencyNode2 adapter = DependencyNode2Adapter.get(node);

      for (List<DependencyNode> conflictGroups : adapter.getConflictingNodeGroups())
      {
         solveConflict(conflictGroups);
      }

      return node;
   }

   private void solveConflict(List<DependencyNode> conflictGroup)
   {
      final DependencyNode choosen = nodeChooser.choose(conflictGroup);
      for (DependencyNode node : conflictGroup)
      {
         if (node == choosen)
         {
            continue;
         }
         DependencyNode2Adapter.get(node).setReplacement(choosen);
         DependencyNode2Adapter.get(choosen).getReplaced().add(node);
      }
   }
}
