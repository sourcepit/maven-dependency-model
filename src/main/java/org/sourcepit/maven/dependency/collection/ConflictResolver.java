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
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.version.Version;
import org.sourcepit.common.maven.model.VersionConflictKey;
import org.sourcepit.common.maven.model.util.MavenModelUtils;

public class ConflictResolver implements TreeProvider<DependencyResolutionNode>
{
   private final Map<Set<VersionConflictKey>, List<DependencyResolutionNode>> conflictGroups = new HashMap<Set<VersionConflictKey>, List<DependencyResolutionNode>>();

   private final TreeProvider<DependencyResolutionNode> target;

   public ConflictResolver(TreeProvider<DependencyResolutionNode> target)
   {
      this.target = target;
   }

   @Override
   public List<DependencyResolutionNode> visitChildren(DependencyResolutionNode parent, int depth,
      List<DependencyResolutionNode> children)
   {
      children = target.visitChildren(parent, depth, children);

      final Map<Set<VersionConflictKey>, List<DependencyResolutionNode>> conflictGroupMap = new HashMap<Set<VersionConflictKey>, List<DependencyResolutionNode>>(
         children.size());

      for (DependencyResolutionNode request : children)
      {
         request.setResolvedVersion(determineResolvedVersion(request));

         final Set<VersionConflictKey> conflictKeys = getConflictKeys(request);

         if (!conflictKeys.isEmpty())
         {
            addItemToConflictGroup(conflictGroupMap, conflictKeys, request);
         }
      }

      for (Entry<Set<VersionConflictKey>, List<DependencyResolutionNode>> entry : conflictGroupMap.entrySet())
      {
         final Set<VersionConflictKey> conflictKeys = entry.getKey();
         final List<DependencyResolutionNode> conflictGroup = entry.getValue();
         if (conflictGroup.size() > 1)
         {
            Collections.sort(conflictGroup, new Comparator<DependencyResolutionNode>()
            {
               @Override
               public int compare(DependencyResolutionNode n1, DependencyResolutionNode n2)
               {
                  return -1 * n1.getResolvedVersion().compareTo(n2.getResolvedVersion());
               }
            });

            // remove winner
            final DependencyResolutionNode winner = conflictGroup.remove(0);

            // remember merged conflict keys, so we won't loose any keys from aliases or relocations of loser nodes
            getConflictKeys(winner).addAll(conflictKeys);

            for (DependencyResolutionNode loser : conflictGroup)
            {
               loser.setConflictNode(winner);
            }
         }
      }

      return children;
   }

   private static Set<VersionConflictKey> getConflictKeys(DependencyResolutionNode request)
   {
      @SuppressWarnings("unchecked")
      Set<VersionConflictKey> conflictKeys = (Set<VersionConflictKey>) request.getData().get("conflictKeys");
      if (conflictKeys == null)
      {
         conflictKeys = determineConflictKeys(request);
         request.getData().put("conflictKeys", conflictKeys);
      }
      return conflictKeys;
   }

   private static Version determineResolvedVersion(DependencyResolutionNode request)
   {
      return getHighestVersion(request.getVersionRangeResult().getVersions());
   }

   private static Set<VersionConflictKey> determineConflictKeys(DependencyResolutionNode request)
   {
      final Map<Version, ArtifactDescriptorResult> versionToDescriptorResultMap = request
         .getVersionToArtifactDescriptorResultMap();
      final Collection<ArtifactDescriptorResult> artifactDescriptorResults = versionToDescriptorResultMap.values();
      final Set<VersionConflictKey> conflictKeys = new HashSet<VersionConflictKey>();
      for (ArtifactDescriptorResult descriptorResult : artifactDescriptorResults)
      {
         conflictKeys.addAll(getConflictKeys(descriptorResult));
      }

      return conflictKeys;
   }

   private static Version getHighestVersion(Collection<Version> versions)
   {
      if (versions.isEmpty())
      {
         return null;
      }
      final Iterator<Version> it = versions.iterator();
      Version max = it.next();
      while (it.hasNext())
      {
         final Version version = (Version) it.next();
         if (max.compareTo(version) < 0)
         {
            max = version;
         }
      }
      return max;
   }

   @Override
   public void leaveChildren(DependencyResolutionNode parent, int depth, List<DependencyResolutionNode> children)
   {
   }

   @Override
   public List<DependencyResolutionNode> getChildren(DependencyResolutionNode parent)
   {
      if (parent.getConflictNode() != null)
      {
         return Collections.emptyList();
      }

      final DependencyResolutionNode cyclicParent = findCycle(parent);
      if (cyclicParent != null)
      {
         parent.setCyclicParent(cyclicParent);
         return Collections.emptyList();
      }

      final Entry<Set<VersionConflictKey>, List<DependencyResolutionNode>> conflictGroup = addItemToConflictGroup(
         conflictGroups, getConflictKeys(parent), parent);

      if (conflictGroup.getValue().size() > 1)
      {
         final Iterator<DependencyResolutionNode> it = conflictGroup.getValue().iterator();
         final DependencyResolutionNode winner = it.next();
         while (it.hasNext())
         {
            it.next().setConflictNode(winner);
         }
         return Collections.emptyList();
      }

      return target.getChildren(parent);
   }

   private static DependencyResolutionNode findCycle(DependencyResolutionNode node)
   {
      DependencyResolutionNode parent = node.getParent();
      while (parent != null && !contains(getConflictKeys(parent), getConflictKeys(node)))
      {
         parent = parent.getParent();
      }
      return parent;
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

   private static Set<VersionConflictKey> getConflictKeys(ArtifactDescriptorResult descriptorResult)
   {
      final Set<VersionConflictKey> conflictKeys = new HashSet<VersionConflictKey>();
      final Artifact artifact = descriptorResult.getArtifact();
      if (artifact != null)
      {
         final VersionConflictKey targetGroupKey = toVersionConflictKey(artifact);
         if (targetGroupKey != null)
         {
            conflictKeys.add(targetGroupKey);

            final Collection<Artifact> aliases = descriptorResult.getAliases();
            if (aliases != null)
            {
               for (Artifact alias : aliases)
               {
                  conflictKeys.add(toVersionConflictKey(alias));
               }
            }

            final List<Artifact> relocations = descriptorResult.getRelocations();
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

   private static VersionConflictKey toVersionConflictKey(final Artifact artifact)
   {
      return MavenModelUtils.toVersionConflictKey(artifact.getGroupId(), artifact.getArtifactId(),
         artifact.getExtension(), artifact.getClassifier());
   }

}
