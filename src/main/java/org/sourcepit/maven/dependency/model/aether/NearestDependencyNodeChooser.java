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

package org.sourcepit.maven.dependency.model.aether;

import java.util.List;

import org.eclipse.aether.graph.DependencyNode;

public class NearestDependencyNodeChooser implements DependencyNodeChooser {
   private boolean skipTest;

   public NearestDependencyNodeChooser(boolean skipTest) {
      this.skipTest = skipTest;
   }

   @Override
   public DependencyNode choose(List<DependencyNode> nodes) {
      final boolean hasVisibles = hasVisibles(nodes);

      DependencyNode chosen = null;
      int chosenDepth = Integer.MAX_VALUE;

      for (DependencyNode node : nodes) {
         final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
         if ((hasVisibles && !adapter.isVisible()) || getRoot(node, adapter) == null) {
            continue;
         }

         String scope = node.getDependency().getScope();
         if (skipTest && "test".equals(scope)) {
            continue;
         }

         final int nodeDepth = adapter.getMinimalDepth();
         if (chosenDepth > nodeDepth) {
            chosen = node;
            chosenDepth = nodeDepth;
         }
      }

      if (chosen == null) {
         for (DependencyNode node : nodes) {
            final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
            final int nodeDepth = adapter.getMinimalDepth();
            if (chosenDepth > nodeDepth) {
               chosen = node;
               chosenDepth = nodeDepth;
            }
         }
      }

      return chosen;
   }

   private boolean hasVisibles(List<DependencyNode> nodes) {
      for (DependencyNode node : nodes) {
         final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
         if (adapter.isVisible()) {
            return true;
         }
      }
      return false;
   }

   private DependencyNode getRoot(DependencyNode node, DependencyNode2 adapter) {
      if (adapter.getParents().isEmpty()) {
         return node;
      }

      for (DependencyNode parent : adapter.getParents()) {
         final DependencyNode2 parentAdapter = DependencyNode2Adapter.get(parent);
         if (parentAdapter.getReplacement() != null) {
            continue;
         }

         final DependencyNode root = getRoot(parent, parentAdapter);
         if (root != null) {
            return root;
         }
      }

      return null;
   }
}
