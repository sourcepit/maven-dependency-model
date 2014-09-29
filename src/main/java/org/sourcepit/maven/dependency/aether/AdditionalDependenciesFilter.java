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

package org.sourcepit.maven.dependency.aether;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.graph.Dependency;
import org.sourcepit.maven.dependency.DependenciesFilter;
import org.sourcepit.maven.dependency.impl.NoopDependenciesFilter;

public final class AdditionalDependenciesFilter implements DependenciesFilter
{
   private final List<Dependency> dependencies;

   public AdditionalDependenciesFilter(List<Dependency> dependencies)
   {
      this.dependencies = dependencies;
   }

   @Override
   public List<Dependency> filterDependencies(Artifact artifact, List<Dependency> deps)
   {
      return mergeDeps(dependencies, deps);
   }

   @Override
   public DependenciesFilter deriveChildFilter(DependencyCollectionContext context)
   {
      return new NoopDependenciesFilter();
   }

   public static List<Dependency> mergeDeps(List<Dependency> dominant, List<Dependency> recessive)
   {
      List<Dependency> result;
      if (dominant == null || dominant.isEmpty())
      {
         result = recessive;
      }
      else if (recessive == null || recessive.isEmpty())
      {
         result = dominant;
      }
      else
      {
         result = new ArrayList<Dependency>(dominant.size() + recessive.size());
         Collection<String> ids = new HashSet<String>();
         for (Dependency dependency : dominant)
         {
            ids.add(getId(dependency.getArtifact()));
            result.add(dependency);
         }
         for (Dependency dependency : recessive)
         {
            if (!ids.contains(getId(dependency.getArtifact())))
            {
               result.add(dependency);
            }
         }
      }
      return result;
   }

   private static String getId(Artifact a)
   {
      return a.getGroupId() + ':' + a.getArtifactId() + ':' + a.getClassifier() + ':' + a.getExtension();
   }
}