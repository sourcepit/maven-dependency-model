/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import org.eclipse.aether.artifact.Artifact;

public class GraphsForArtifactRequest extends AbstractGraphsRequest
{
   private Artifact artifact;

   public Artifact getArtifact()
   {
      return artifact;
   }

   public void setArtifact(Artifact artifact)
   {
      this.artifact = artifact;
   }
}