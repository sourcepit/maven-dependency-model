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

package org.sourcepit.maven.dependency.model;

import org.apache.maven.artifact.Artifact;

public class DependencyTreeBuilderRequest {
   private Artifact artifact;

   private boolean excludeOptionalDependencies;

   public void setArtifact(Artifact artifact) {
      this.artifact = artifact;
   }

   public Artifact getArtifact() {
      return artifact;
   }

   public void setExcludeOptionalDependencies(boolean excludeOptionalDependencies) {
      this.excludeOptionalDependencies = excludeOptionalDependencies;
   }

   public boolean isExcludeOptionalDependencies() {
      return excludeOptionalDependencies;
   }
}
