/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.aether;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionConstraint;

public class DependencyNodeImpl implements DependencyNode
{
   private String requestContext;

   private Dependency dependency;

   private List<Artifact> relocations;

   private Collection<Artifact> aliases;

   private VersionConstraint versionConstraint;

   private Version version;

   private int managedBits;

   private List<RemoteRepository> repositories;

   private Map<Object, Object> data;

   private List<DependencyNode> children;

   @Override
   public void setRequestContext(String requestContext)
   {
      this.requestContext = requestContext;
   }

   public String getRequestContext()
   {
      if (requestContext == null)
      {
         requestContext = "";
      }
      return requestContext;
   }

   public void setDependency(Dependency dependency)
   {
      this.dependency = dependency;
   }

   public Dependency getDependency()
   {
      return dependency;
   }

   public void setRelocations(List<Artifact> relocations)
   {
      this.relocations = relocations;
   }

   public List<Artifact> getRelocations()
   {
      if (relocations == null)
      {
         relocations = new ArrayList<Artifact>(0);
      }
      return relocations;
   }

   public void setAliases(Collection<Artifact> aliases)
   {
      this.aliases = aliases;
   }

   public Collection<Artifact> getAliases()
   {
      if (aliases == null)
      {
         aliases = new ArrayList<Artifact>(0);
      }
      return aliases;
   }

   public void setVersionConstraint(VersionConstraint versionConstraint)
   {
      this.versionConstraint = versionConstraint;
   }

   public VersionConstraint getVersionConstraint()
   {
      return versionConstraint;
   }

   public void setVersion(Version version)
   {
      this.version = version;
   }

   public Version getVersion()
   {
      return version;
   }

   public void setManagedBits(int managedBits)
   {
      this.managedBits = (byte) (managedBits & 0x1F);
   }

   public int getManagedBits()
   {
      return managedBits;
   }

   public void setRepositories(List<RemoteRepository> repositories)
   {
      this.repositories = repositories;
   }

   @Override
   public List<RemoteRepository> getRepositories()
   {
      if (repositories == null)
      {
         repositories = new ArrayList<RemoteRepository>(0);
      }
      return repositories;
   }

   @Override
   public void setData(Map<Object, Object> data)
   {
      this.data = data;
   }

   @Override
   public void setData(Object key, Object value)
   {
      getData().put(key, value);
   }

   public Map<Object, Object> getData()
   {
      if (data == null)
      {
         data = new HashMap<Object, Object>();
      }
      return data;
   }

   public List<DependencyNode> getChildren()
   {
      if (children == null)
      {
         children = new ArrayList<DependencyNode>(0);
      }
      return children;
   }

   @Override
   public void setChildren(List<DependencyNode> children)
   {
      this.children = children;
   }

   @Override
   public Artifact getArtifact()
   {
      return dependency == null ? null : dependency.getArtifact();
   }

   @Override
   public void setArtifact(Artifact artifact)
   {
      dependency = dependency.setArtifact(artifact);
   }

   @Override
   public void setScope(String scope)
   {
      dependency = dependency.setScope(scope);
   }

   @Override
   public void setOptional(Boolean optional)
   {
      dependency = dependency.setOptional(optional);
   }

   @Override
   public boolean accept(DependencyVisitor visitor)
   {
      if (visitor.visitEnter(this))
      {
         for (DependencyNode child : children)
         {
            if (!child.accept(visitor))
            {
               break;
            }
         }
      }
      return visitor.visitLeave(this);
   }

   @Override
   public String toString()
   {
      final Dependency dep = getDependency();
      if (dep == null)
      {
         return String.valueOf(getArtifact());
      }
      return dep.toString();
   }


}
