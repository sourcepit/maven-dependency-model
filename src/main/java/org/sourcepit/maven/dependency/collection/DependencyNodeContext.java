/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;

public class DependencyNodeContext
{
   private final RepositorySystemSession session;

   private final RemoteRepositoryManager remoteRepositoryManager;

   private DependencySelector dependencySelector;

   private DependencyManager dependencyManager;

   private DependencyTraverser dependencyTraverser;

   private DependenciesFilter dependenciesFilter;

   private boolean savePremanagedState;

   private String requestContext;

   private RequestTrace requestTrace;

   private List<RemoteRepository> repositories;

   private LinkedList<DependencyNode> parentNodes;

   public DependencyNodeContext(RepositorySystemSession session, RemoteRepositoryManager remoteRepositoryManager)
   {
      this.session = session;
      this.remoteRepositoryManager = remoteRepositoryManager;
   }
   
   public RepositorySystemSession getSession()
   {
      return session;
   }

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

   public void setSavePremanagedState(boolean savePremanagedState)
   {
      this.savePremanagedState = savePremanagedState;
   }

   public boolean isSavePremanagedState()
   {
      return savePremanagedState;
   }

   public void setRequestContext(String requestContext)
   {
      this.requestContext = requestContext;
   }

   public String getRequestContext()
   {
      return requestContext;
   }

   public RequestTrace getRequestTrace()
   {
      return requestTrace;
   }

   public void setRequestTrace(RequestTrace requestTrace)
   {
      this.requestTrace = requestTrace;
   }

   public List<RemoteRepository> getRepositories()
   {
      if (repositories == null)
      {
         repositories = new ArrayList<RemoteRepository>(0);
      }
      return repositories;
   }

   public void setRepositories(List<RemoteRepository> repositories)
   {
      this.repositories = repositories;
   }

   public void setParentNodes(LinkedList<DependencyNode> parentNodes)
   {
      this.parentNodes = parentNodes;
   }

   public LinkedList<DependencyNode> getParentNodes()
   {
      if (parentNodes == null)
      {
         parentNodes = new LinkedList<DependencyNode>();
      }
      return parentNodes;
   }

   public DependencyNodeContext deriveChildContext(DependencyNodeImpl parentNode, List<Dependency> managedDependencies,
      List<RemoteRepository> repositories)
   {
      final DefaultDependencyCollectionContext collectionContext = new DefaultDependencyCollectionContext(session,
         parentNode.getDependency(), managedDependencies);

      final DependencyNodeContext childContext = new DependencyNodeContext(session, remoteRepositoryManager);
      childContext.getParentNodes().addAll(this.getParentNodes());
      childContext.getParentNodes().add(parentNode);

      childContext.setSavePremanagedState(this.isSavePremanagedState());

      childContext.setDependencySelector(this.getDependencySelector().deriveChildSelector(collectionContext));
      childContext.setDependencyManager(this.getDependencyManager().deriveChildManager(collectionContext));
      childContext.setDependencyTraverser(this.getDependencyTraverser().deriveChildTraverser(collectionContext));

      childContext.setDependenciesFilter(this.getDependenciesFilter().deriveChildFilter(collectionContext));

      childContext.setRequestContext(this.getRequestContext());
      childContext.setRequestTrace(this.getRequestTrace());


      final List<RemoteRepository> childRepos;
      if (session.isIgnoreArtifactDescriptorRepositories())
      {
         childRepos = this.getRepositories();
      }
      else
      {
         childRepos = remoteRepositoryManager
            .aggregateRepositories(session, this.getRepositories(), repositories, true);
      }

      childContext.setRepositories(childRepos);

      return childContext;
   }

}