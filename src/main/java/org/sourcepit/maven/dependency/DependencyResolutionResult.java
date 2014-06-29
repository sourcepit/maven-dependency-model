/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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