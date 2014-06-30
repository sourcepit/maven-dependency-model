/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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