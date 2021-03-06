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

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.common.maven.model.VersionConflictKey;
import org.sourcepit.common.maven.model.util.MavenModelUtils;

public class DependencyNode2Adapter implements DependencyNode2 {
   private final DependencyNode target;

   private final Set<DependencyNode> parents = new LinkedHashSet<DependencyNode>();

   private final Set<DependencyNode> replaced = new LinkedHashSet<DependencyNode>();

   private int minimalDepth = -1;

   private DependencyNode replacement;

   private boolean visible = true;

   public DependencyNode2Adapter(DependencyNode target) {
      this.target = target;
   }

   @Override
   public DependencyNode getTarget() {
      return target;
   }

   @Override
   public Set<DependencyNode> getParents() {
      return parents;
   }

   @Override
   public int getMinimalDepth() {
      return minimalDepth;
   }

   public void setMinimalDepth(int minimalDepth) {
      this.minimalDepth = minimalDepth;
   }

   @Override
   public DependencyNode getReplacement() {
      return replacement;
   }

   public void setReplacement(DependencyNode replacement) {
      this.replacement = replacement;
   }

   public Set<DependencyNode> getReplaced() {
      return replaced;
   }

   private Collection<List<DependencyNode>> conflictGroups;

   @Override
   public Collection<List<DependencyNode>> getConflictingNodeGroups() {
      if (conflictGroups == null) {
         conflictGroups = DependencyUtils.computeConflictingNodeGroups(this.getTarget());
      }
      return conflictGroups;
   }

   @Override
   public Set<VersionConflictKey> getConflictKeys() {
      final Set<VersionConflictKey> conflictKeys = new HashSet<VersionConflictKey>();

      final VersionConflictKey targetGroupKey = getArtifactConflictKey();
      if (targetGroupKey != null) {
         conflictKeys.add(targetGroupKey);

         // Havn't found any use case for aliases... Even not in aether tests.
         checkState(target.getAliases() == null || target.getAliases().isEmpty());
         // final Collection<Artifact> aliases = node.getAliases();
         // if (aliases != null)
         // {
         // for (Artifact alias : aliases)
         // {
         // conflictGroups.put(targetKey, toVersionConflictKey(alias));
         // }
         // }

         final List<? extends Artifact> relocations = target.getRelocations();
         if (relocations != null) {
            for (Artifact relocation : relocations) {
               conflictKeys.add(toVersionConflictKey(relocation));
            }
         }
      }

      return conflictKeys;
   }

   @Override
   public VersionConflictKey getDependencyConflictKey() {
      final List<? extends Artifact> relocations = target.getRelocations();
      if (relocations != null && !relocations.isEmpty()) {
         return toVersionConflictKey(relocations.get(0));
      }
      return getArtifactConflictKey();
   }

   @Override
   public VersionConflictKey getArtifactConflictKey() {
      final Dependency dependency = target.getDependency();
      return dependency == null ? null : toVersionConflictKey(dependency.getArtifact());
   }

   private static VersionConflictKey toVersionConflictKey(final Artifact artifact) {
      return MavenModelUtils.toVersionConflictKey(artifact.getGroupId(), artifact.getArtifactId(),
         artifact.getExtension(), artifact.getClassifier());
   }

   @Override
   public String toString() {
      if (replacement != null) {
         return target.toString() + " -> " + replacement;
      }
      return target.toString();
   }

   public static DependencyNode2 adapt(DependencyNode root, final boolean reset) {
      final AbstractDependencyVisitor initializor = new AbstractDependencyVisitor(false) {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node) {
            DependencyNode2Adapter node2 = (DependencyNode2Adapter) get(node);
            if (reset && node2 != null) {
               node.setData(DependencyNode2Adapter.class, null);
               node2 = null;
            }

            if (node2 == null) {
               node2 = new DependencyNode2Adapter(node);
               node.setData(DependencyNode2Adapter.class, node2);
            }

            if (parent != null) {
               node2.getParents().add(parent);
            }

            final int depth = path.size();
            final int current = node2.getMinimalDepth();
            if (current == -1 || current > depth) {
               node2.setMinimalDepth(depth);
            }

            return true;
         }
      };

      root.accept(initializor);

      return get(root);
   }

   public static DependencyNode2 get(DependencyNode node) {
      return (DependencyNode2) node.getData().get(DependencyNode2Adapter.class);
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   @Override
   public boolean isVisible() {
      return visible;
   }
}
