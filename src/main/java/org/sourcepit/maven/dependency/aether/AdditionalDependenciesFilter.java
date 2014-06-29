/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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