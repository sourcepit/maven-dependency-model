/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.impl;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sourcepit.maven.dependency.ConflictKeyAdapter;
import org.sourcepit.maven.dependency.DependencyNode;
import org.sourcepit.maven.dependency.DependencyNodeRequest;
import org.sourcepit.maven.dependency.TreeProvider;

public class ConflictSolvingTreeProvider<ConflictKey> extends AbstractConflictSolvingTreeProvider
{
   private final Map<ConflictKey, List<DependencyNode>> conflictGroups = new HashMap<ConflictKey, List<DependencyNode>>();

   private final ConflictKeyAdapter<ConflictKey> conflictKeyAdapter;

   public ConflictSolvingTreeProvider(TreeProvider<DependencyNodeRequest> target,
      ConflictKeyAdapter<ConflictKey> conflictKeyAdapter)
   {
      super(target);
      this.conflictKeyAdapter = conflictKeyAdapter;
   }

   @Override
   protected List<DependencyNodeRequest> solveSiblingConflicts(List<DependencyNodeRequest> siblingRequests)
   {
      final Map<ConflictKey, List<DependencyNode>> conflictGroupMap = new HashMap<ConflictKey, List<DependencyNode>>(
         siblingRequests.size());

      for (DependencyNodeRequest request : siblingRequests)
      {
         final DependencyNode node = request.getNode();
         final ConflictKey key = conflictKeyAdapter.getConflictKey(node);
         addItemToConflictGroup(conflictGroupMap, key, node);
      }

      for (Entry<ConflictKey, List<DependencyNode>> entry : conflictGroupMap.entrySet())
      {
         final ConflictKey groupKey = entry.getKey();
         final List<DependencyNode> conflictGroup = entry.getValue();
         if (conflictGroup.size() > 1)
         {
            Collections.sort(conflictGroup, new Comparator<DependencyNode>()
            {
               @Override
               public int compare(DependencyNode n1, DependencyNode n2)
               {
                  return -1 * n1.getResolvedVersion().compareTo(n2.getResolvedVersion());
               }
            });

            // remove winner
            final DependencyNode winner = conflictGroup.remove(0);

            // remember merged conflict keys, so we won't loose any keys from aliases or relocations of loser nodes
            conflictKeyAdapter.mergeConflictKeys(winner, groupKey);

            for (DependencyNode loser : conflictGroup)
            {
               loser.setConflictNode(winner);
            }
         }
      }

      return siblingRequests;
   }

   @Override
   protected void updateTreeConflicts(DependencyNode node)
   {
      final Entry<ConflictKey, List<DependencyNode>> conflictGroup = addItemToConflictGroup(conflictGroups,
         conflictKeyAdapter.getConflictKey(node), node);

      if (conflictGroup.getValue().size() > 1)
      {
         final Iterator<DependencyNode> it = conflictGroup.getValue().iterator();
         final DependencyNode winner = it.next();
         while (it.hasNext())
         {
            it.next().setConflictNode(winner);
         }
      }
   }

   private <T> Entry<ConflictKey, List<T>> addItemToConflictGroup(Map<ConflictKey, List<T>> conflictGroupMap,
      ConflictKey key, T item)
   {
      final List<Entry<ConflictKey, List<T>>> groups = getConflictGroups(conflictGroupMap, key);

      if (groups.isEmpty())
      {
         final List<T> group = new ArrayList<T>(1);
         group.add(item);
         conflictGroupMap.put(key, group);
         return new SimpleImmutableEntry<ConflictKey, List<T>>(key, group);
      }
      else
      {
         ConflictKey groupKey = key;
         final List<T> group = new ArrayList<T>(1);

         for (Entry<ConflictKey, List<T>> entry : groups)
         {
            conflictGroupMap.remove(entry.getKey());

            groupKey = conflictKeyAdapter.mergeConflictKeys(groupKey, entry.getKey());
            group.addAll(entry.getValue());
         }

         group.add(item);
         conflictGroupMap.put(groupKey, group);
         return new SimpleImmutableEntry<ConflictKey, List<T>>(groupKey, group);
      }
   }

   private <T> List<Entry<ConflictKey, T>> getConflictGroups(Map<ConflictKey, T> conflictGroupMap, ConflictKey key)
   {
      final Set<Entry<ConflictKey, T>> entrySet = conflictGroupMap.entrySet();

      final List<Entry<ConflictKey, T>> conflictGroups = new ArrayList<Entry<ConflictKey, T>>(1);

      for (Entry<ConflictKey, T> entry : entrySet)
      {
         final ConflictKey conflictGroup = entry.getKey();
         if (conflictKeyAdapter.conflicts(conflictGroup, key))
         {
            conflictGroups.add(entry);
         }
      }

      return conflictGroups;
   }

}
