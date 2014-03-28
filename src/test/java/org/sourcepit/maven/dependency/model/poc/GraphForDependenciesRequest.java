/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import java.util.List;

import org.eclipse.aether.graph.Dependency;

public class GraphForDependenciesRequest extends AbstractGraphRequest
{
   private List<Dependency> dependencies;

   public List<Dependency> getDependencies()
   {
      return dependencies;
   }

   public void setDependencies(List<Dependency> dependencies)
   {
      this.dependencies = dependencies;
   }
}