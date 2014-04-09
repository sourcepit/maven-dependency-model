/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import java.util.Collection;

import org.sourcepit.common.constraints.NotNull;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.sourcepit.maven.dependency.model.DependencyModel;


// TODO exceptions handling...
public interface DependencyModelResolver
{
   DependencyModel resolve(@NotNull Collection<Dependency> dependencies, ArtifactAttachmentFactory attachmentFactory)
      throws ProjectBuildingException, DependencyResolutionException;

   DependencyModel resolve(@NotNull Artifact artifact, ArtifactAttachmentFactory attachmentFactory)
      throws ProjectBuildingException, DependencyResolutionException;

   DependencyModel resolve(@NotNull MavenProject project, ArtifactAttachmentFactory attachmentFactory)
      throws DependencyResolutionException;

}