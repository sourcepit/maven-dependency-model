/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class AbstractGraphRequest
{
   private List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();
   private List<Dependency> managedDependencies = new ArrayList<Dependency>();
   private DependencyResolutionScope dependencyResolutionScope;

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