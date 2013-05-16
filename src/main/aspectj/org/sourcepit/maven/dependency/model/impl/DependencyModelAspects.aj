/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.impl;

import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyTree;

public aspect DependencyModelAspects
{
   pointcut getArtifact(DependencyModel model, ArtifactKey artifactKey): target(model) && args(artifactKey) && execution(MavenArtifact getArtifact(ArtifactKey));
   
   MavenArtifact around(DependencyModel model, ArtifactKey artifactKey) : getArtifact(model, artifactKey){
      return DependencyModelOperations.getArtifact(model, artifactKey);
   }
   
   pointcut getDependencyTree(DependencyModel model, MavenArtifact artifact): target(model) && args(artifact) && execution(DependencyTree getDependencyTree(MavenArtifact));

   DependencyTree around(DependencyModel model, MavenArtifact artifact) : getDependencyTree(model, artifact){
      return DependencyModelOperations.getDependencyTree(model, artifact.getArtifactKey());
   }
   
   pointcut getDependencyTree1(DependencyModel model, ArtifactKey artifactKey): target(model) && args(artifactKey) && execution(DependencyTree getDependencyTree(ArtifactKey));

   DependencyTree around(DependencyModel model, ArtifactKey artifactKey) : getDependencyTree1(model, artifactKey){
      return DependencyModelOperations.getDependencyTree(model, artifactKey);
   }
}
