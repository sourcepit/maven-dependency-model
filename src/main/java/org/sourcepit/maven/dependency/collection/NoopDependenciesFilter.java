/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.graph.Dependency;

public class NoopDependenciesFilter implements DependenciesFilter
{
   @Override
   public List<Dependency> filterDependencies(Artifact artifact, List<Dependency> dependencies)
   {
      return dependencies;
   }

   @Override
   public DependenciesFilter deriveChildFilter(DependencyCollectionContext context)
   {
      return this;
   }
}
