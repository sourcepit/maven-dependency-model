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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

public class DependencyNode
{
   private final DependencyNode parent;

   private final String requestContext;

   private List<RemoteRepository> repositories;

   private final Dependency dependency;

   private final Map<Object, Object> data = new HashMap<Object, Object>();

   private ManagedDependency managedDependency;

   private VersionRangeResult versionRangeResult;

   private Map<Version, ArtifactDescriptorResult> versionToArtifactDescriptorResultMap;

   private Version resolvedVersion;

   private DependencyNode cyclicParent;

   private DependencyNode conflictNode;

   public DependencyNode(String requestContext, List<RemoteRepository> repositories, Dependency dependency)
   {
      this(null, requestContext, repositories, dependency);
   }

   public DependencyNode(DependencyNode parent, List<RemoteRepository> repositories, Dependency dependency)
   {
      this(parent, parent.getRequestContext(), repositories, dependency);
   }

   private DependencyNode(DependencyNode parent, String requestContext, List<RemoteRepository> repositories,
      Dependency dependency)
   {
      this.parent = parent;
      this.requestContext = requestContext;
      this.repositories = repositories;
      this.dependency = dependency;
   }

   public DependencyNode getParent()
   {
      return parent;
   }

   public String getRequestContext()
   {
      return requestContext;
   }

   public Dependency getDependency()
   {
      return dependency;
   }

   public List<RemoteRepository> getRepositories()
   {
      return repositories;
   }

   public Map<Object, Object> getData()
   {
      return data;
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

   public void setVersionToArtifactDescriptorResultMap(
      Map<Version, ArtifactDescriptorResult> versionToArtifactDescriptorResultMap)
   {
      this.versionToArtifactDescriptorResultMap = versionToArtifactDescriptorResultMap;
   }

   public Map<Version, ArtifactDescriptorResult> getVersionToArtifactDescriptorResultMap()
   {
      return versionToArtifactDescriptorResultMap;
   }

   public void setResolvedVersion(Version resolvedVersion)
   {
      this.resolvedVersion = resolvedVersion;
   }

   public Version getResolvedVersion()
   {
      return resolvedVersion;
   }

   public void setCyclicParent(DependencyNode cyclicParent)
   {
      this.cyclicParent = cyclicParent;
   }

   public DependencyNode getCyclicParent()
   {
      return cyclicParent;
   }

   public void setConflictNode(DependencyNode conflictNode)
   {
      this.conflictNode = conflictNode;
   }

   public DependencyNode getConflictNode()
   {
      return conflictNode;
   }
}