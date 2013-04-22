/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import org.apache.maven.artifact.Artifact;

public class DependencyTreeBuilderRequest
{
   private Artifact artifact;

   private boolean excludeOptionalDependencies;

   public void setArtifact(Artifact artifact)
   {
      this.artifact = artifact;
   }

   public Artifact getArtifact()
   {
      return artifact;
   }

   public void setExcludeOptionalDependencies(boolean excludeOptionalDependencies)
   {
      this.excludeOptionalDependencies = excludeOptionalDependencies;
   }

   public boolean isExcludeOptionalDependencies()
   {
      return excludeOptionalDependencies;
   }
}
