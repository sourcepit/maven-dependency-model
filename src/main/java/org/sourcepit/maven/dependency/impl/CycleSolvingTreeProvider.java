/*
 * Copyright 2014 Bernd Vogt and others.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sourcepit.maven.dependency.impl;

import java.util.Collections;
import java.util.List;

import org.sourcepit.maven.dependency.ConflictKeyAdapter;
import org.sourcepit.maven.dependency.DependencyNode;
import org.sourcepit.maven.dependency.DependencyNodeRequest;
import org.sourcepit.maven.dependency.TreeProvider;

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
      final DependencyNode node = request.getNode();
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
         final DependencyNode node = request.getNode();
         node.setCyclicParent(detectCyclicParent(node));
      }
      return requests;
   }

   protected DependencyNode detectCyclicParent(DependencyNode node)
   {
      DependencyNode parent = node.getParent();
      while (parent != null && !conflictKeyAdapter.conflicts(parent, node))
      {
         parent = parent.getParent();
      }
      return parent;
   }

}
