/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.graph.Dependency;

public class DependencyNodeContext
{
   private DependencySelector dependencySelector;

   private DependencyManager dependencyManager;

   private DependencyTraverser dependencyTraverser;

   private DependenciesFilter dependenciesFilter;

   public void setDependencySelector(DependencySelector dependencySelector)
   {
      this.dependencySelector = dependencySelector;
   }

   public DependencySelector getDependencySelector()
   {
      return dependencySelector;
   }

   public void setDependencyManager(DependencyManager dependencyManager)
   {
      this.dependencyManager = dependencyManager;
   }

   public DependencyManager getDependencyManager()
   {
      return dependencyManager;
   }

   public void setDependencyTraverser(DependencyTraverser dependencyTraverser)
   {
      this.dependencyTraverser = dependencyTraverser;
   }

   public DependencyTraverser getDependencyTraverser()
   {
      return dependencyTraverser;
   }

   public void setDependenciesFilter(DependenciesFilter dependenciesFilter)
   {
      this.dependenciesFilter = dependenciesFilter;
   }

   public DependenciesFilter getDependenciesFilter()
   {
      return dependenciesFilter;
   }

   public DependencyNodeContext deriveChildContext(RepositorySystemSession session, Dependency parent,
      List<Dependency> managedDependencies)
   {
      final DefaultDependencyCollectionContext collectionContext = new DefaultDependencyCollectionContext(session,
         parent, managedDependencies);

      final DependencyNodeContext childContext = new DependencyNodeContext();

      childContext.setDependencySelector(this.getDependencySelector().deriveChildSelector(collectionContext));
      childContext.setDependencyManager(this.getDependencyManager().deriveChildManager(collectionContext));
      childContext.setDependencyTraverser(this.getDependencyTraverser().deriveChildTraverser(collectionContext));
      childContext.setDependenciesFilter(this.getDependenciesFilter().deriveChildFilter(collectionContext));

      return childContext;
   }

}