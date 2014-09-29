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

public final class DependencyModelOperations
{
   private DependencyModelOperations()
   {
      super();
   }

   public static MavenArtifact getArtifact(DependencyModel model, ArtifactKey artifactKey)
   {
      for (MavenArtifact artifact : model.getArtifacts())
      {
         if (artifactKey.equals(artifact.getArtifactKey()))
         {
            return artifact;
         }
      }
      return null;
   }

   public static DependencyTree getDependencyTree(DependencyModel model, ArtifactKey artifactKey)
   {
      for (DependencyTree tree : model.getDependencyTrees())
      {
         final MavenArtifact artifact = tree.getArtifact();
         if (artifact != null && artifactKey.equals(artifact.getArtifactKey()))
         {
            return tree;
         }
      }
      return null;
   }
}
