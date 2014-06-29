/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.impl;

import java.util.Collections;
import java.util.List;

import org.sourcepit.maven.dependency.DependencyNode;
import org.sourcepit.maven.dependency.DependencyNodeRequest;
import org.sourcepit.maven.dependency.TreeProvider;

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
      return solveSiblingConflicts(target.getRoots(requests));
   }

   @Override
   public List<DependencyNodeRequest> getChildren(DependencyNodeRequest request)
   {
      final DependencyNode node = request.getNode();
      if (node.getConflictNode() != null)
      {
         return Collections.emptyList();
      }
      return solveTreeConflicts(solveSiblingConflicts(target.getChildren(request)));
   }

   protected abstract List<DependencyNodeRequest> solveSiblingConflicts(List<DependencyNodeRequest> requests);

   protected List<DependencyNodeRequest> solveTreeConflicts(List<DependencyNodeRequest> requests)
   {
      for (DependencyNodeRequest request : requests)
      {
         final DependencyNode node = request.getNode();
         if (node.getConflictNode() == null) // no sibling conflicts
         {
            updateTreeConflicts(node);
         }
      }
      return requests;
   }

   protected abstract void updateTreeConflicts(DependencyNode node);
}
