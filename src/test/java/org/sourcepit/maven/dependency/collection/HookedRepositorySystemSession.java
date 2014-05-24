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
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.graph.Dependency;

public class HookedRepositorySystemSession extends AbstractForwardingRepositorySystemSession
{
   private final List<Dependency> selectDependencyCalls = new ArrayList<Dependency>();

   private int deriveChildSelectorCalls = 0;

   private final List<Dependency> manageDependencyCalls = new ArrayList<Dependency>();

   private int deriveChildManagerCalls = 0;

   private final List<Dependency> traverseDependencyCalls = new ArrayList<Dependency>();

   private int deriveChildTraverserCalls = 0;

   private final RepositorySystemSession session;

   public HookedRepositorySystemSession(RepositorySystemSession session)
   {
      this.session = session;
   }

   public List<Dependency> getSelectDependencyCalls()
   {
      return selectDependencyCalls;
   }

   public int getDeriveChildSelectorCalls()
   {
      return deriveChildSelectorCalls;
   }

   public List<Dependency> getManageDependencyCalls()
   {
      return manageDependencyCalls;
   }

   public int getDeriveChildManagerCalls()
   {
      return deriveChildManagerCalls;
   }

   public List<Dependency> getTraverseDependencyCalls()
   {
      return traverseDependencyCalls;
   }

   public int getDeriveChildTraverserCalls()
   {
      return deriveChildTraverserCalls;
   }

   @Override
   protected RepositorySystemSession getSession()
   {
      return session;
   }

   @Override
   public DependencySelector getDependencySelector()
   {
      return new DependencySelectorRecorder(getSession().getDependencySelector());
   }

   @Override
   public DependencyManager getDependencyManager()
   {
      return new DependencyManagerRecorder(super.getDependencyManager());
   }

   @Override
   public DependencyTraverser getDependencyTraverser()
   {
      return new DependencyTraverserRecorder(super.getDependencyTraverser());
   }

   private class DependencySelectorRecorder implements DependencySelector
   {
      private final DependencySelector dependencySelector;

      public DependencySelectorRecorder(DependencySelector dependencySelector)
      {
         this.dependencySelector = dependencySelector;
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
         deriveChildSelectorCalls++;
         return new DependencySelectorRecorder(dependencySelector.deriveChildSelector(context));
      }
   }

   private class DependencyManagerRecorder implements DependencyManager
   {
      private final DependencyManager dependencyManager;

      public DependencyManagerRecorder(DependencyManager dependencyManager)
      {
         this.dependencyManager = dependencyManager;
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
         deriveChildManagerCalls++;
         return new DependencyManagerRecorder(dependencyManager.deriveChildManager(context));
      }
   }

   private class DependencyTraverserRecorder implements DependencyTraverser
   {
      private final DependencyTraverser dependencyTraverser;

      public DependencyTraverserRecorder(DependencyTraverser dependencyTraverser)
      {
         this.dependencyTraverser = dependencyTraverser;
      }

      @Override
      public boolean traverseDependency(Dependency dependency)
      {
         traverseDependencyCalls.add(dependency);
         return dependencyTraverser.traverseDependency(dependency);
      }

      @Override
      public DependencyTraverser deriveChildTraverser(DependencyCollectionContext context)
      {
         deriveChildTraverserCalls++;
         return new DependencyTraverserRecorder(dependencyTraverser.deriveChildTraverser(context));
      }
   }
}