/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sourcepit.common.maven.model.VersionConflictKey;
import org.sourcepit.common.maven.model.util.MavenModelUtils;

public class DependencyNode2Adapter implements DependencyNode2
{
   private final DependencyNode target;

   private final Set<DependencyNode> parents = new LinkedHashSet<DependencyNode>();

   private final Set<DependencyNode> replaced = new LinkedHashSet<DependencyNode>();

   private int minimalDepth = -1;

   private DependencyNode replacement;

   private boolean visible = true;

   public DependencyNode2Adapter(DependencyNode target)
   {
      this.target = target;
   }

   @Override
   public DependencyNode getTarget()
   {
      return target;
   }

   @Override
   public Set<DependencyNode> getParents()
   {
      return parents;
   }

   @Override
   public int getMinimalDepth()
   {
      return minimalDepth;
   }

   public void setMinimalDepth(int minimalDepth)
   {
      this.minimalDepth = minimalDepth;
   }

   @Override
   public DependencyNode getReplacement()
   {
      return replacement;
   }

   public void setReplacement(DependencyNode replacement)
   {
      this.replacement = replacement;
   }

   public Set<DependencyNode> getReplaced()
   {
      return replaced;
   }

   private Collection<List<DependencyNode>> conflictGroups;

   @Override
   public Collection<List<DependencyNode>> getConflictingNodeGroups()
   {
      if (conflictGroups == null)
      {
         conflictGroups = DependencyUtils.computeConflictingNodeGroups(this.getTarget());
      }
      return conflictGroups;
   }

   @Override
   public Set<VersionConflictKey> getConflictKeys()
   {
      final Set<VersionConflictKey> conflictKeys = new HashSet<VersionConflictKey>();

      final VersionConflictKey targetGroupKey = getArtifactConflictKey();
      if (targetGroupKey != null)
      {
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

         final List<Artifact> relocations = target.getRelocations();
         if (relocations != null)
         {
            for (Artifact relocation : relocations)
            {
               conflictKeys.add(toVersionConflictKey(relocation));
            }
         }
      }

      return conflictKeys;
   }

   @Override
   public VersionConflictKey getDependencyConflictKey()
   {
      final List<Artifact> relocations = target.getRelocations();
      if (relocations != null && !relocations.isEmpty())
      {
         return toVersionConflictKey(relocations.get(0));
      }
      return getArtifactConflictKey();
   }

   @Override
   public VersionConflictKey getArtifactConflictKey()
   {
      final Dependency dependency = target.getDependency();
      return dependency == null ? null : toVersionConflictKey(dependency.getArtifact());
   }

   private static VersionConflictKey toVersionConflictKey(final Artifact artifact)
   {
      return MavenModelUtils.toVersionConflictKey(artifact.getGroupId(), artifact.getArtifactId(),
         artifact.getExtension(), artifact.getClassifier());
   }

   @Override
   public String toString()
   {
      if (replacement != null)
      {
         return target.toString() + " -> " + replacement;
      }
      return target.toString();
   }

   public static DependencyNode2 adapt(DependencyNode root, final boolean reset)
   {
      final AbstractDependencyVisitor initializor = new AbstractDependencyVisitor(false)
      {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            DependencyNode2Adapter node2 = (DependencyNode2Adapter) get(node);
            if (reset && node2 != null)
            {
               node.setData(DependencyNode2Adapter.class, null);
               node2 = null;
            }

            if (node2 == null)
            {
               node2 = new DependencyNode2Adapter(node);
               node.setData(DependencyNode2Adapter.class, node2);
            }

            if (parent != null)
            {
               node2.getParents().add(parent);
            }

            final int depth = path.size();
            final int current = node2.getMinimalDepth();
            if (current == -1 || current > depth)
            {
               node2.setMinimalDepth(depth);
            }

            return true;
         }
      };

      root.accept(initializor);

      return get(root);
   }

   public static DependencyNode2 get(DependencyNode node)
   {
      return (DependencyNode2) node.getData().get(DependencyNode2Adapter.class);
   }

   @Override
   public void setVisible(boolean visible)
   {
      this.visible = visible;
   }

   @Override
   public boolean isVisible()
   {
      return visible;
   }
}