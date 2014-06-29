/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.Collections;
import java.util.List;

public class CycleSolvingTreeProvider<ConflictKey> implements TreeProvider<DependencyNodeRequest>
{
   private final TreeProvider<DependencyNodeRequest> target;

   private final ConflictKeyAdapter<ConflictKey> conflictKeyAdapter;

   public CycleSolvingTreeProvider(TreeProvider<DependencyNodeRequest> target,
      ConflictKeyAdapter<ConflictKey> conflictKeyAdapter)
   {
      this.target = target;
      this.conflictKeyAdapter = conflictKeyAdapter;
   }

   @Override
   public List<DependencyNodeRequest> getRoots(List<DependencyNodeRequest> roots)
   {
      return solveCycles(target.getRoots(roots));
   }

   @Override
   public List<DependencyNodeRequest> getChildren(DependencyNodeRequest request)
   {
      final DependencyResolutionNode node = request.getNode();
      if (node.getCyclicParent() != null)
      {
         return Collections.emptyList();
      }
      return solveCycles(target.getChildren(request));
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

   protected DependencyResolutionNode detectCyclicParent(DependencyResolutionNode node)
   {
      DependencyResolutionNode parent = node.getParent();
      while (parent != null && !conflictKeyAdapter.conflicts(parent, node))
      {
         parent = parent.getParent();
      }
      return parent;
   }

}
