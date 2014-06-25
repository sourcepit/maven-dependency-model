/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.Collections;
import java.util.List;

public abstract class AbstractConflictSolvingTreeProvider implements TreeProvider<DependencyResolutionNode>
{
   private final TreeProvider<DependencyResolutionNode> target;

   public AbstractConflictSolvingTreeProvider(TreeProvider<DependencyResolutionNode> target)
   {
      this.target = target;
   }

   @Override
   public List<DependencyResolutionNode> getRoots(List<DependencyResolutionNode> roots)
   {
      return solveSiblingConflicts(solveCycles(target.getRoots(roots)));
   }

   @Override
   public List<DependencyResolutionNode> getChildren(DependencyResolutionNode node)
   {
      if (node.getConflictNode() != null)
      {
         return Collections.emptyList();
      }

      if (node.getCyclicParent() != null)
      {
         return Collections.emptyList();
      }
      return solveTreeConflicts(solveSiblingConflicts(solveCycles(target.getChildren(node))));
   }

   protected List<DependencyResolutionNode> solveCycles(List<DependencyResolutionNode> nodes)
   {
      for (DependencyResolutionNode node : nodes)
      {
         node.setCyclicParent(detectCyclicParent(node));
      }
      return nodes;
   }

   protected abstract DependencyResolutionNode detectCyclicParent(DependencyResolutionNode node);

   protected abstract List<DependencyResolutionNode> solveSiblingConflicts(List<DependencyResolutionNode> nodes);

   protected List<DependencyResolutionNode> solveTreeConflicts(List<DependencyResolutionNode> nodes)
   {
      for (DependencyResolutionNode node : nodes)
      {
         if (node.getConflictNode() == null) // no sibling conflicts
         {
            updateTreeConflicts(node);
         }
      }
      return nodes;
   }

   protected abstract void updateTreeConflicts(DependencyResolutionNode node);
}
