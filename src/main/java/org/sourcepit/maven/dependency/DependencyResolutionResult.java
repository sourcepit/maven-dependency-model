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

package org.sourcepit.maven.dependency;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

public class DependencyResolutionResult
{
   private final DependencyResolutionRequest request;

   private ManagedDependency managedDependency;

   private VersionRangeResult versionRangeResult;

   private Map<Version, ArtifactDescriptorResult> versionToArtifactDescriptorResultMap;

   public DependencyResolutionResult(DependencyResolutionRequest request)
   {
      this.request = request;
   }

   public DependencyResolutionRequest getRequest()
   {
      return request;
   }

   public void setManagedDependency(ManagedDependency managedDependency)
   {
      this.managedDependency = managedDependency;
   }

   public ManagedDependency getManagedDependency()
   {
      return managedDependency;
   }

   public void setVersionRangeResult(VersionRangeResult versionRangeResult)
   {
      this.versionRangeResult = versionRangeResult;
   }

   public VersionRangeResult getVersionRangeResult()
   {
      return versionRangeResult;
   }

   public Map<Version, ArtifactDescriptorResult> getVersionToArtifactDescriptorResultMap()
   {
      if (versionToArtifactDescriptorResultMap == null)
      {
         versionToArtifactDescriptorResultMap = new LinkedHashMap<Version, ArtifactDescriptorResult>(1);
      }
      return versionToArtifactDescriptorResultMap;
   }
}