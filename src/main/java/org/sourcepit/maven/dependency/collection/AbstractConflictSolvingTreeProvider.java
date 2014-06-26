/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.Collections;
import java.util.List;

public abstract class AbstractConflictSolvingTreeProvider implements TreeProvider<DependencyNodeRequest>
{
   private final TreeProvider<DependencyNodeRequest> target;

   public AbstractConflictSolvingTreeProvider(TreeProvider<DependencyNodeRequest> target)
   {
      this.target = target;
   }

   @Override
   public List<DependencyNodeRequest> getRoots(List<DependencyNodeRequest> requests)
   {
      return solveSiblingConflicts(solveCycles(target.getRoots(requests)));
   }

   @Override
   public List<DependencyNodeRequest> getChildren(DependencyNodeRequest request)
   {
      final DependencyResolutionNode node = request.getNode();

      if (node.getConflictNode() != null)
      {
         return Collections.emptyList();
      }

      if (node.getCyclicParent() != null)
      {
         return Collections.emptyList();
      }

      return solveTreeConflicts(solveSiblingConflicts(solveCycles(target.getChildren(request))));
   }

   protected List<DependencyNodeRequest> solveCycles(List<DependencyNodeRequest> requests)
   {
      for (DependencyNodeRequest request : requests)
      {
         final DependencyResolutionNode node = request.getNode();
         node.setCyclicParent(detectCyclicParent(node));
      }
      return requests;
   }

   protected abstract DependencyResolutionNode detectCyclicParent(DependencyResolutionNode node);

   protected abstract List<DependencyNodeRequest> solveSiblingConflicts(List<DependencyNodeRequest> requests);

   protected List<DependencyNodeRequest> solveTreeConflicts(List<DependencyNodeRequest> requests)
   {
      for (DependencyNodeRequest request : requests)
      {
         final DependencyResolutionNode node = request.getNode();
         if (node.getConflictNode() == null) // no sibling conflicts
         {
            updateTreeConflicts(node);
         }
      }
      return requests;
   }

   protected abstract void updateTreeConflicts(DependencyResolutionNode node);
}
