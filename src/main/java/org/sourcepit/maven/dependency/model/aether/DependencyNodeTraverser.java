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

import static org.sourcepit.common.maven.artifact.MavenArtifactUtils.toArtifactKey;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;

public class DependencyNodeTraverser {
   private final Stack<ArtifactKey> parentKeys = new Stack<ArtifactKey>();

   private final Stack<DependencyNode> parents = new Stack<DependencyNode>();

   private final boolean followReplacements;

   public DependencyNodeTraverser(boolean followReplacements) {
      this.followReplacements = followReplacements;
   }


   public DependencyNodeTraverser() {
      this(true);
   }

   public void traverse(DependencyNode node) {
      traverse(null, null, node, new LinkedPropertiesMap());
   }

   private boolean traverse(ArtifactKey parentKey, DependencyNode parent, final DependencyNode node,
      PropertiesMap parentProps) {
      if (parent != null) {
         parentKeys.push(parentKey);
         parents.push(parent);
      }
      try {
         final DependencyNode effectiveNode = getEffectiveNode(parent, node);
         final List<DependencyNode> path = Collections.unmodifiableList(parents);
         final List<DependencyNode> selectedChildren = getChildren(path, parent, node, effectiveNode);
         final ArtifactKey nodeKey = getNodeKey(selectedChildren, parent, node, effectiveNode);
         final int parentIdx = parentKeys.indexOf(nodeKey);
         final DependencyNode cycleNode = parentIdx > -1 ? path.get(parentIdx) : null;

         final DependencyNode rootNode = path.isEmpty() ? node : path.get(0);

         final boolean replaced = node != effectiveNode;
         final boolean followReplacements = this.followReplacements
            || (replaced && !isParentNodeOf(rootNode, effectiveNode));

         final boolean skipChildren = cycleNode != null || replaced && !followReplacements;

         final Visit visit = new Visit(path, parent, node, effectiveNode, selectedChildren, cycleNode);
         if (visitEnter(visit, parentProps) && !skipChildren) {
            for (DependencyNode childNode : selectedChildren) {
               if (!traverse(nodeKey, effectiveNode, childNode, new LinkedPropertiesMap(parentProps))) {
                  break;
               }
            }
         }
         return visitLeave(visit, parentProps);
      }
      finally {
         if (parent != null) {
            parents.pop();
            parentKeys.pop();
         }
      }
   }

   private boolean isParentNodeOf(DependencyNode parent, DependencyNode node) {
      if (parent.equals(node)) {
         return true;
      }

      final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
      for (DependencyNode parentNode : adapter.getParents()) {
         if (isParentNodeOf(parent, parentNode)) {
            return true;
         }
      }

      return false;
   }

   protected boolean visitEnter(Visit visit, PropertiesMap parentProps) {
      return true;
   }

   protected boolean visitLeave(Visit visit, PropertiesMap parentProps) {
      return true;
   }

   protected DependencyNode getEffectiveNode(DependencyNode parent, DependencyNode node) {
      return getEffectiveNode(node);
   }

   protected DependencyNode getEffectiveNode(DependencyNode node) {
      final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
      final DependencyNode replacement = adapter == null ? null : adapter.getReplacement();
      if (replacement == null) {
         return node;
      }
      return getEffectiveNode(replacement);
   }

   protected ArtifactKey getNodeKey(List<DependencyNode> parents, DependencyNode parent, DependencyNode node,
      final DependencyNode effectiveNode) {
      final Dependency dependency = effectiveNode.getDependency();
      if (dependency != null) {
         return toArtifactKey(dependency.getArtifact());
      }
      return null;
   }

   protected List<DependencyNode> getChildren(List<DependencyNode> parents, DependencyNode parent, DependencyNode node,
      final DependencyNode effectiveNode) {
      return effectiveNode.getChildren();
   }

   public static class Visit {
      private final List<DependencyNode> path;
      private final DependencyNode parent;
      private final DependencyNode node;
      private final DependencyNode effectiveNode;
      private final List<DependencyNode> selectedChildren;
      private final DependencyNode cycleNode;

      public Visit(List<DependencyNode> path, DependencyNode parent, DependencyNode node, DependencyNode effectiveNode,
         List<DependencyNode> selectedChildren, DependencyNode cycleNode) {
         this.path = path;
         this.parent = parent;
         this.node = node;
         this.effectiveNode = effectiveNode;
         this.selectedChildren = selectedChildren;
         this.cycleNode = cycleNode;
      }

      public DependencyNode getRootNode() {
         return path.isEmpty() ? node : path.get(0);
      }

      public boolean isRoot() {
         return parent == null;
      }

      public boolean isReplaced() {
         return node != effectiveNode;
      }

      public boolean isCyclic() {
         return cycleNode != null;
      }

      public List<DependencyNode> getPath() {
         return path;
      }

      public DependencyNode getParent() {
         return parent;
      }

      public DependencyNode getNode() {
         return node;
      }

      public DependencyNode getEffectiveNode() {
         return effectiveNode;
      }

      public DependencyNode getReplacementNode() {
         return isReplaced() ? effectiveNode : null;
      }

      public List<DependencyNode> getSelectedChildren() {
         return selectedChildren;
      }

      public DependencyNode getCycleNode() {
         return cycleNode;
      }

      @Override
      public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append(getNode());
         if (isReplaced()) {
            sb.append(" -> ");
            sb.append(getEffectiveNode());
         }
         if (isCyclic()) {
            sb.append(" (cyclic)");
         }
         return sb.toString();
      }
   }
}
