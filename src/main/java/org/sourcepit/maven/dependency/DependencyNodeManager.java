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

package org.sourcepit.maven.dependency;

import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencyManagement;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.graph.Dependency;
import org.sourcepit.maven.dependency.aether.DependencyCollectionContextImpl;

public class DependencyNodeManager
   implements
      DependencySelector,
      DependencyManager,
      DependencyTraverser,
      DependenciesFilter
{
   private final DependencySelector dependencySelector;

   private final DependencyManager dependencyManager;

   private final DependencyTraverser dependencyTraverser;

   private final DependenciesFilter dependenciesFilter;

   public DependencyNodeManager(DependencySelector dependencySelector, DependencyManager dependencyManager,
      DependencyTraverser dependencyTraverser, DependenciesFilter dependenciesFilter)
   {
      this.dependencySelector = dependencySelector;
      this.dependencyManager = dependencyManager;
      this.dependencyTraverser = dependencyTraverser;
      this.dependenciesFilter = dependenciesFilter;
   }

   public DependencyNodeManager deriveChildManager(RepositorySystemSession session, Dependency parent,
      List<Dependency> managedDependencies)
   {
      final DependencyCollectionContextImpl collectionContext = new DependencyCollectionContextImpl(session, parent,
         managedDependencies);

      return new DependencyNodeManager(dependencySelector.deriveChildSelector(collectionContext),
         dependencyManager.deriveChildManager(collectionContext),
         dependencyTraverser.deriveChildTraverser(collectionContext),
         dependenciesFilter.deriveChildFilter(collectionContext));
   }

   @Override
   public List<Dependency> filterDependencies(Artifact artifact, List<Dependency> dependencies)
   {
      return dependenciesFilter.filterDependencies(artifact, dependencies);
   }

   @Override
   public DependenciesFilter deriveChildFilter(DependencyCollectionContext context)
   {
      return dependenciesFilter.deriveChildFilter(context);
   }

   @Override
   public boolean traverseDependency(Dependency dependency)
   {
      return dependencyTraverser.traverseDependency(dependency);
   }

   @Override
   public DependencyTraverser deriveChildTraverser(DependencyCollectionContext context)
   {
      return dependencyTraverser.deriveChildTraverser(context);
   }

   @Override
   public DependencyManagement manageDependency(Dependency dependency)
   {
      return dependencyManager.manageDependency(dependency);
   }

   @Override
   public DependencyManager deriveChildManager(DependencyCollectionContext context)
   {
      return dependencyManager.deriveChildManager(context);
   }

   @Override
   public boolean selectDependency(Dependency dependency)
   {
      return dependencySelector.selectDependency(dependency);
   }

   @Override
   public DependencySelector deriveChildSelector(DependencyCollectionContext context)
   {
      return dependencySelector.deriveChildSelector(context);
   }

}