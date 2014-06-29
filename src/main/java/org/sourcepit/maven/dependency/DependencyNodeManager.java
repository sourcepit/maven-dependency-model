/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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