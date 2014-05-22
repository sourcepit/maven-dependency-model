/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencyManagement;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;

public final class HookedRepositorySystemSession extends AbstractForwardingRepositorySystemSession
{
   private final List<Dependency> selectDependencyCalls = new ArrayList<Dependency>();

   private final List<Dependency> manageDependencyCalls = new ArrayList<Dependency>();

   private final RepositorySystemSession session;

   public HookedRepositorySystemSession(RepositorySystemSession session)
   {
      this.session = session;
   }

   public List<Dependency> getSelectDependencyCalls()
   {
      return selectDependencyCalls;
   }
   
   public List<Dependency> getManageDependencyCalls()
   {
      return manageDependencyCalls;
   }

   @Override
   protected RepositorySystemSession getSession()
   {
      return session;
   }

   @Override
   public DependencySelector getDependencySelector()
   {
      return new DependencySelectorRecorder(getSession().getDependencySelector(), selectDependencyCalls);
   }

   @Override
   public DependencyManager getDependencyManager()
   {
      return new DependencyManagerRecorder(super.getDependencyManager(), manageDependencyCalls);
   }

   private static class DependencySelectorRecorder implements DependencySelector
   {
      private final DependencySelector dependencySelector;
      private final List<Dependency> selectDependencyCalls;

      public DependencySelectorRecorder(DependencySelector dependencySelector, List<Dependency> selectDependencyCalls)
      {
         this.dependencySelector = dependencySelector;
         this.selectDependencyCalls = selectDependencyCalls;
      }

      @Override
      public boolean selectDependency(Dependency dependency)
      {
         selectDependencyCalls.add(dependency);
         return dependencySelector.selectDependency(dependency);
      }

      @Override
      public DependencySelector deriveChildSelector(DependencyCollectionContext context)
      {
         return new DependencySelectorRecorder(dependencySelector.deriveChildSelector(context), selectDependencyCalls);
      }
   }

   private static class DependencyManagerRecorder implements DependencyManager
   {
      private final DependencyManager dependencyManager;
      private final List<Dependency> manageDependencyCalls;

      public DependencyManagerRecorder(DependencyManager dependencyManager, List<Dependency> manageDependencyCalls)
      {
         this.dependencyManager = dependencyManager;
         this.manageDependencyCalls = manageDependencyCalls;
      }

      @Override
      public DependencyManagement manageDependency(Dependency dependency)
      {
         manageDependencyCalls.add(dependency);
         return dependencyManager.manageDependency(dependency);
      }

      @Override
      public DependencyManager deriveChildManager(DependencyCollectionContext context)
      {
         return new DependencyManagerRecorder(dependencyManager.deriveChildManager(context), manageDependencyCalls);
      }
   }
}