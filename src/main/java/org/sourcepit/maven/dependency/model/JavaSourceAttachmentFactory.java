/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import java.util.Collections;
import java.util.Set;

import org.sourcepit.common.maven.model.ArtifactKey;

public class JavaSourceAttachmentFactory implements ArtifactAttachmentFactory
{
   @Override
   public Set<ArtifactAttachment> createAttachments(ArtifactKey artifact)
   {
      if (isAttachJavaSource(artifact))
      {
         return Collections.singleton(new ArtifactAttachment(null, "java-source", isJavaSourceRequired(artifact)));
      }
      return null;
   }

   protected boolean isJavaSourceRequired(ArtifactKey artifact)
   {
      return false;
   }

   protected boolean isAttachJavaSource(ArtifactKey artifact)
   {
      return artifact.getClassifier() == null && "jar".equals(artifact.getType());
   }
}
