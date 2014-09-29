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