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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.version.Version;
import org.sourcepit.common.maven.model.VersionConflictKey;
import org.sourcepit.common.maven.model.util.MavenModelUtils;
import org.sourcepit.maven.dependency.ConflictKeyAdapter;
import org.sourcepit.maven.dependency.DependencyNode;

public class VersionConflictKeyAdapter implements ConflictKeyAdapter<Set<VersionConflictKey>>
{
   @Override
   public Set<VersionConflictKey> getConflictKey(DependencyNode node)
   {
      @SuppressWarnings("unchecked")
      Set<VersionConflictKey> conflictKeys = (Set<VersionConflictKey>) node.getData().get(
         VersionConflictKeyAdapter.class);
      if (conflictKeys == null)
      {
         conflictKeys = determineConflictKeys(node);
         node.getData().put(VersionConflictKeyAdapter.class, conflictKeys);
      }
      return conflictKeys;
   }

   @Override
   public Set<VersionConflictKey> mergeConflictKeys(Set<VersionConflictKey> key1, Set<VersionConflictKey> key2)
   {
      final Set<VersionConflictKey> merged = new HashSet<VersionConflictKey>(key1.size() + key2.size());
      merged.addAll(key1);
      merged.addAll(key2);
      return merged;
   }

   @Override
   public Set<VersionConflictKey> mergeConflictKeys(DependencyNode node, Set<VersionConflictKey> key)
   {
      final Set<VersionConflictKey> merged = getConflictKey(node);
      merged.addAll(key);
      return merged;
   }

   public boolean conflicts(Set<VersionConflictKey> key1, Set<VersionConflictKey> key2)
   {
      for (VersionConflictKey conflictKey : key1)
      {
         if (key2.contains(conflictKey))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean conflicts(DependencyNode node1, DependencyNode node2)
   {
      return conflicts(getConflictKey(node1), getConflictKey(node2));
   }

   private static Set<VersionConflictKey> determineConflictKeys(DependencyNode node)
   {
      final Map<Version, ArtifactDescriptorResult> versionToDescriptorResultMap = node
         .getVersionToArtifactDescriptorResultMap();
      final Collection<ArtifactDescriptorResult> artifactDescriptorResults = versionToDescriptorResultMap.values();
      final Set<VersionConflictKey> conflictKeys = new HashSet<VersionConflictKey>();
      for (ArtifactDescriptorResult descriptorResult : artifactDescriptorResults)
      {
         conflictKeys.addAll(getConflictKeys(descriptorResult));
      }

      return conflictKeys;
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
