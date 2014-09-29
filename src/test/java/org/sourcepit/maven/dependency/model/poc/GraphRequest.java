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

package org.sourcepit.maven.dependency.model.poc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class GraphRequest
{
   private Artifact rootArtifact;
   private List<Dependency> dependencies = new ArrayList<Dependency>();
   private List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();
   private List<Dependency> managedDependencies = new ArrayList<Dependency>();
   private DependencyResolutionScope dependencyResolutionScope;

   public Artifact getRootArtifact()
   {
      return rootArtifact;
   }

   public void setRootArtifact(Artifact rootArtifact)
   {
      this.rootArtifact = rootArtifact;
   }

   public List<Dependency> getDependencies()
   {
      return dependencies;
   }

   public void setDependencies(List<Dependency> dependencies)
   {
      this.dependencies = dependencies;
   }

   public List<RemoteRepository> getRepositories()
   {
      return repositories;
   }

   public List<Dependency> getManagedDependencies()
   {
      return managedDependencies;
   }

   public DependencyResolutionScope getDependencyResolutionScope()
   {
      return dependencyResolutionScope;
   }

   public void setDependencyResolutionScope(DependencyResolutionScope dependencyResolutionScope)
   {
      this.dependencyResolutionScope = dependencyResolutionScope;
   }

}