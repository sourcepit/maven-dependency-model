/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.common.maven.model.VersionConflictKey;
import org.sourcepit.common.maven.model.util.MavenModelUtils;

public class ConflictResolver implements TreeProvider<DependencyNodeRequest>
{
   private final Map<Set<VersionConflictKey>, List<DependencyNode>> conflictGroups = new HashMap<Set<VersionConflictKey>, List<DependencyNode>>();

   private final TreeProvider<DependencyNodeRequest> target;

   public ConflictResolver(TreeProvider<DependencyNodeRequest> target)
   {
      this.target = target;
   }

   @Override
   public List<DependencyNodeRequest> visitChildren(DependencyNodeRequest parent, int depth,
      List<DependencyNodeRequest> children)
   {
      children = target.visitChildren(parent, depth, children);

      final Map<Set<VersionConflictKey>, List<DependencyNode>> conflictGroupMap = new HashMap<Set<VersionConflictKey>, List<DependencyNode>>(
         children.size());

      for (DependencyNodeRequest request : children)
      {
         final DependencyNode node = request.getDependencyNode();
         if (node != null)
         {
            addItemToConflictGroup(conflictGroupMap, getConflictKeys(node), node);
         }
      }

      final Set<DependencyNode> losers = new HashSet<DependencyNode>();
      for (Entry<Set<VersionConflictKey>, List<DependencyNode>> entry : conflictGroupMap.entrySet())
      {
         final Set<VersionConflictKey> conflictKeys = entry.getKey();
         final List<DependencyNode> conflictGroup = entry.getValue();
         if (conflictGroup.size() > 1)
         {
            Collections.sort(conflictGroup, new Comparator<DependencyNode>()
            {
               @Override
               public int compare(DependencyNode n1, DependencyNode n2)
               {
                  return -1 * n1.getVersion().compareTo(n2.getVersion());
               }
            });

            // remove winner
            final DependencyNode winner = conflictGroup.remove(0);

            // remember merged conflict keys, so we won't loose any keys from aliases or relocations of loser nodes
            setConflictKeys(winner, conflictKeys);

            losers.addAll(conflictGroup);
         }
      }

      for (Iterator<DependencyNodeRequest> it = children.iterator(); it.hasNext();)
      {
         final DependencyNodeRequest request = (DependencyNodeRequest) it.next();
         if (losers.contains(request.getDependencyNode()))
         {
            it.remove();
         }
      }

      return children;
   }

   @Override
   public void leaveChildren(DependencyNodeRequest parent, int depth, List<DependencyNodeRequest> children)
   {
   }

   @Override
   public List<DependencyNodeRequest> getChildren(DependencyNodeRequest parent)
   {
      final DependencyNode node = parent.getDependencyNode();
      if (node != null)
      {
         final Entry<Set<VersionConflictKey>, List<DependencyNode>> conflictGroup = addItemToConflictGroup(
            conflictGroups, getConflictKeys(node), node);
         if (conflictGroup.getValue().size() > 1)
         {
            return Collections.emptyList();
         }
      }
      return target.getChildren(parent);

   }

   private static <T> Entry<Set<VersionConflictKey>, List<T>> addItemToConflictGroup(
      Map<Set<VersionConflictKey>, List<T>> conflictGroupMap, Set<VersionConflictKey> conflictKeys, T item)
   {
      final List<Entry<Set<VersionConflictKey>, List<T>>> groups = getConflictGroups(conflictGroupMap, conflictKeys);

      if (groups.isEmpty())
      {
         final List<T> group = new ArrayList<T>(1);
         group.add(item);
         conflictGroupMap.put(conflictKeys, group);
         return new SimpleImmutableEntry<Set<VersionConflictKey>, List<T>>(conflictKeys, group);
      }
      else
      {
         final Set<VersionConflictKey> groupKey = new HashSet<VersionConflictKey>();
         final List<T> group = new ArrayList<T>(1);

         for (Entry<Set<VersionConflictKey>, List<T>> entry : groups)
         {
            conflictGroupMap.remove(entry.getKey());

            groupKey.addAll(entry.getKey());
            group.addAll(entry.getValue());
         }

         groupKey.addAll(conflictKeys);
         group.add(item);
         conflictGroupMap.put(groupKey, group);
         return new SimpleImmutableEntry<Set<VersionConflictKey>, List<T>>(groupKey, group);
      }
   }

   private static <T> List<Entry<Set<VersionConflictKey>, T>> getConflictGroups(
      Map<Set<VersionConflictKey>, T> conflictGroupMap, Set<VersionConflictKey> conflictKeys)
   {
      final Set<Entry<Set<VersionConflictKey>, T>> entrySet = conflictGroupMap.entrySet();

      final List<Entry<Set<VersionConflictKey>, T>> conflictGroups = new ArrayList<Entry<Set<VersionConflictKey>, T>>(1);

      for (Entry<Set<VersionConflictKey>, T> entry : entrySet)
      {
         final Set<VersionConflictKey> conflictGroup = entry.getKey();
         if (contains(conflictGroup, conflictKeys))
         {
            conflictGroups.add(entry);
         }
      }

      return conflictGroups;
   }

   private static boolean contains(Set<VersionConflictKey> conflictGroup, Set<VersionConflictKey> conflictKeys)
   {
      for (VersionConflictKey conflictKey : conflictKeys)
      {
         if (conflictGroup.contains(conflictKey))
         {
            return true;
         }
      }
      return false;
   }

   public static Set<VersionConflictKey> getConflictKeys(DependencyNode dependencyNode)
   {
      @SuppressWarnings("unchecked")
      Set<VersionConflictKey> conflictKeys = (Set<VersionConflictKey>) dependencyNode.getData().get("conflictKeys");
      if (conflictKeys == null)
      {
         conflictKeys = new HashSet<VersionConflictKey>();
         setConflictKeys(dependencyNode, conflictKeys);

         final VersionConflictKey targetGroupKey = getArtifactConflictKey(dependencyNode);
         if (targetGroupKey != null)
         {
            conflictKeys.add(targetGroupKey);

            final Collection<Artifact> aliases = dependencyNode.getAliases();
            if (aliases != null)
            {
               for (Artifact alias : aliases)
               {
                  conflictKeys.add(toVersionConflictKey(alias));
               }
            }

            final List<Artifact> relocations = dependencyNode.getRelocations();
            if (relocations != null)
            {
               for (Artifact relocation : relocations)
               {
                  conflictKeys.add(toVersionConflictKey(relocation));
               }
            }
         }
      }
      return conflictKeys;
   }

   private static void setConflictKeys(DependencyNode dependencyNode, Set<VersionConflictKey> conflictKeys)
   {
      dependencyNode.setData("conflictKeys", conflictKeys);
   }

   public static VersionConflictKey getArtifactConflictKey(DependencyNode dependencyNode)
   {
      final Dependency dependency = dependencyNode.getDependency();
      return getArtifactConflictKey(dependency);
   }

   private static VersionConflictKey getArtifactConflictKey(final Dependency dependency)
   {
      return dependency == null ? null : toVersionConflictKey(dependency.getArtifact());
   }

   private static VersionConflictKey toVersionConflictKey(final Artifact artifact)
   {
      return MavenModelUtils.toVersionConflictKey(artifact.getGroupId(), artifact.getArtifactId(),
         artifact.getExtension(), artifact.getClassifier());
   }

}
