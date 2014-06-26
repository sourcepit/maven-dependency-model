/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencyManagement;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.graph.Dependency;

public class DependencyNodeManager
   implements
      DependencySelector,
      DependencyManager,
      DependencyTraverser,
      DependenciesFilter
{
   private DependencySelector dependencySelector;

   private DependencyManager dependencyManager;

   private DependencyTraverser dependencyTraverser;

   private DependenciesFilter dependenciesFilter;

   public void setDependencySelector(DependencySelector dependencySelector)
   {
      this.dependencySelector = dependencySelector;
   }

   private DependencySelector getDependencySelector()
   {
      return dependencySelector;
   }

   public void setDependencyManager(DependencyManager dependencyManager)
   {
      this.dependencyManager = dependencyManager;
   }

   private DependencyManager getDependencyManager()
   {
      return dependencyManager;
   }

   public void setDependencyTraverser(DependencyTraverser dependencyTraverser)
   {
      this.dependencyTraverser = dependencyTraverser;
   }

   private DependencyTraverser getDependencyTraverser()
   {
      return dependencyTraverser;
   }

   public void setDependenciesFilter(DependenciesFilter dependenciesFilter)
   {
      this.dependenciesFilter = dependenciesFilter;
   }

   private DependenciesFilter getDependenciesFilter()
   {
      return dependenciesFilter;
   }

   public DependencyNodeManager deriveChildManager(RepositorySystemSession session, Dependency parent,
      List<Dependency> managedDependencies)
   {
      final DefaultDependencyCollectionContext collectionContext = new DefaultDependencyCollectionContext(session,
         parent, managedDependencies);

      final DependencyNodeManager childContext = new DependencyNodeManager();

      childContext.setDependencySelector(this.getDependencySelector().deriveChildSelector(collectionContext));
      childContext.setDependencyManager(this.getDependencyManager().deriveChildManager(collectionContext));
      childContext.setDependencyTraverser(this.getDependencyTraverser().deriveChildTraverser(collectionContext));
      childContext.setDependenciesFilter(this.getDependenciesFilter().deriveChildFilter(collectionContext));

      return childContext;
   }

   @Override
   public List<Dependency> filterDependencies(Artifact artifact, List<Dependency> dependencies)
   {
      return getDependenciesFilter().filterDependencies(artifact, dependencies);
   }

   @Override
   public DependenciesFilter deriveChildFilter(DependencyCollectionContext context)
   {
      return getDependenciesFilter().deriveChildFilter(context);
   }

   @Override
   public boolean traverseDependency(Dependency dependency)
   {
      return getDependencyTraverser().traverseDependency(dependency);
   }

   @Override
   public DependencyTraverser deriveChildTraverser(DependencyCollectionContext context)
   {
      return getDependencyTraverser().deriveChildTraverser(context);
   }

   @Override
   public DependencyManagement manageDependency(Dependency dependency)
   {
      return getDependencyManager().manageDependency(dependency);
   }

   @Override
   public DependencyManager deriveChildManager(DependencyCollectionContext context)
   {
      return getDependencyManager().deriveChildManager(context);
   }

   @Override
   public boolean selectDependency(Dependency dependency)
   {
      return getDependencySelector().selectDependency(dependency);
   }

   @Override
   public DependencySelector deriveChildSelector(DependencyCollectionContext context)
   {
      return getDependencySelector().deriveChildSelector(context);
   }

}