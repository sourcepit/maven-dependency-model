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
