/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.impl;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.sourcepit.maven.dependency.model.DependencyModel;


public interface DependencyModelResolver
{

   public abstract DependencyModel resolve(@NotNull Collection<Dependency> dependencies)
      throws ProjectBuildingException, DependencyResolutionException;

   public abstract DependencyModel resolve(@NotNull Artifact artifact) throws ProjectBuildingException,
      DependencyResolutionException;

   public abstract DependencyModel resolve(@NotNull MavenProject project) throws DependencyResolutionException;

}