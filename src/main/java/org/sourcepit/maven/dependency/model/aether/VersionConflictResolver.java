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

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.DependencyNode;

public class VersionConflictResolver implements DependencyGraphTransformer {
   private DependencyNodeChooser nodeChooser;

   public VersionConflictResolver(DependencyNodeChooser nodeChooser) {
      this.nodeChooser = nodeChooser;
   }

   @Override
   public DependencyNode transformGraph(DependencyNode node, DependencyGraphTransformationContext context)
      throws RepositoryException {
      final DependencyNode2 adapter = DependencyNode2Adapter.get(node);

      for (List<DependencyNode> conflictGroups : adapter.getConflictingNodeGroups()) {
         solveConflict(conflictGroups);
      }

      return node;
   }

   private void solveConflict(List<DependencyNode> conflictGroup) {
      final DependencyNode choosen = nodeChooser.choose(conflictGroup);
      for (DependencyNode node : conflictGroup) {
         if (node == choosen) {
            continue;
         }
         DependencyNode2Adapter.get(node).setReplacement(choosen);
         DependencyNode2Adapter.get(choosen).getReplaced().add(node);
      }
   }
}
