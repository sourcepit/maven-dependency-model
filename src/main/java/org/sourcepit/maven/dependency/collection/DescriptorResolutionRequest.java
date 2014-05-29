/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class DescriptorResolutionRequest
{
   private Dependency dependency;
   private RepositorySystemSession session;
   private DependencyManager dependencyManager;
   private DependencySelector dependencySelector;
   private String requestContext;
   private RequestTrace requestTrace;
   private List<RemoteRepository> repositories;

   public Dependency getDependency()
   {
      return dependency;
   }

   public void setDependency(Dependency dependency)
   {
      this.dependency = dependency;
   }

   public RepositorySystemSession getSession()
   {
      return session;
   }

   public void setSession(RepositorySystemSession session)
   {
      this.session = session;
   }

   public DependencyManager getDependencyManager()
   {
      return dependencyManager;
   }

   public void setDependencyManager(DependencyManager dependencyManager)
   {
      this.dependencyManager = dependencyManager;
   }

   public DependencySelector getDependencySelector()
   {
      return dependencySelector;
   }

   public void setDependencySelector(DependencySelector dependencySelector)
   {
      this.dependencySelector = dependencySelector;
   }

   public String getRequestContext()
   {
      return requestContext;
   }

   public void setRequestContext(String requestContext)
   {
      this.requestContext = requestContext;
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
         this.repositories = new ArrayList<RemoteRepository>(1);
      }
      return repositories;
   }

   public void setRepositories(List<RemoteRepository> repositories)
   {
      this.repositories = repositories;
   }

}