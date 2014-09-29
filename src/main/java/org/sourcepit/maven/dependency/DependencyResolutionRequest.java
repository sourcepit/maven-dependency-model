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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class DependencyResolutionRequest
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