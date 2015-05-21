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

import java.util.Stack;

import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

public abstract class AbstractDependencyVisitor implements DependencyVisitor {
   protected Stack<DependencyNode> path = new Stack<DependencyNode>();

   protected boolean filterInvisibleNodes;

   public AbstractDependencyVisitor(boolean filterInvisibleNodes) {
      this.filterInvisibleNodes = filterInvisibleNodes;
   }

   private boolean skipLeave;

   @Override
   public final boolean visitEnter(DependencyNode node) {
      if (filterInvisibleNodes && !DependencyNode2Adapter.get(node).isVisible()) {
         path.push(node);
         skipLeave = true;
         return false;
      }

      final boolean cycle = path.contains(node);
      if (cycle) {
         path.push(node);
         skipLeave = true;
         return false;
      }
      else {
         final boolean result = onVisitEnter(getParent(), node);
         path.push(node);
         return result;
      }
   }

   private DependencyNode getParent() {
      return path.isEmpty() ? null : path.peek();
   }

   protected boolean onVisitEnter(DependencyNode parent, DependencyNode node) {
      return true;
   }

   @Override
   public final boolean visitLeave(DependencyNode node) {
      path.pop();
      if (skipLeave) {
         skipLeave = false;
         return true;
      }
      return onVisitLeave(getParent(), node);
   }

   protected boolean onVisitLeave(DependencyNode parent, DependencyNode node) {
      return true;
   }
}
