/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

public class DependencyResolutionNode
{
   private final DependencyNodeContext context;

   private final DependencyResolutionNode parent;

   private final Dependency dependency;

   private final Map<Object, Object> data = new HashMap<Object, Object>();

   private ManagedDependency managedDependency;

   private VersionRangeResult versionRangeResult;

   private Map<Version, ArtifactDescriptorResult> versionToArtifactDescriptorResultMap;

   private Version resolvedVersion;

   private DependencyResolutionNode cyclicParent;
   
   private DependencyResolutionNode conflictNode;

   public DependencyResolutionNode(DependencyNodeContext context, DependencyResolutionNode parent, Dependency dependency)
   {
      this.context = context;
      this.parent = parent;
      this.dependency = dependency;
   }

   public DependencyNodeContext getContext()
   {
      return context;
   }

   public DependencyResolutionNode getParent()
   {
      return parent;
   }

   public Dependency getDependency()
   {
      return dependency;
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

   public void setCyclicParent(DependencyResolutionNode cyclicParent)
   {
      this.cyclicParent = cyclicParent;
   }

   public DependencyResolutionNode getCyclicParent()
   {
      return cyclicParent;
   }

   public void setConflictNode(DependencyResolutionNode conflictNode)
   {
      this.conflictNode = conflictNode;
   }
   
   public DependencyResolutionNode getConflictNode()
   {
      return conflictNode;
   }
}