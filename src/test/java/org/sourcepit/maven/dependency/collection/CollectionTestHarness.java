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

package org.sourcepit.maven.dependency.collection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Relocation;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Assert;
import org.sourcepit.common.maven.artifact.ArtifactFactory;
import org.sourcepit.common.maven.model.ArtifactKey;

public final class CollectionTestHarness
{
   private CollectionTestHarness()
   {
      super();
   }

   public static void assertDependencyNodeEquals(DependencyNode expected, DependencyNode actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }
      assertNotNull(actual);

      Assert.assertEquals(expected.getRequestContext(), actual.getRequestContext());
      assertArtifactsEquals(expected.getAliases(), actual.getAliases());
      assertArtifactEquals(expected.getArtifact(), actual.getArtifact());
      assertEquals(expected.getDependency(), expected.getDependency());
      Assert.assertEquals(expected.getManagedBits(), actual.getManagedBits());
      assertArtifactsEquals(expected.getRelocations(), actual.getRelocations());
      assertRemoteRepositoriesEquals(expected.getRepositories(), actual.getRepositories());
      Assert.assertEquals(expected.getVersion(), actual.getVersion());
      Assert.assertEquals(expected.getVersionConstraint(), actual.getVersionConstraint());

      assertDependencyNodesEquals(expected.getChildren(), actual.getChildren());
   }

   public static void assertDependencyNodesEquals(List<DependencyNode> expected, List<DependencyNode> actual)
   {
      Assert.assertEquals(expected.size(), actual.size());
      final Iterator<DependencyNode> expectedIt = expected.iterator();
      final Iterator<DependencyNode> actualIt = actual.iterator();
      while (expectedIt.hasNext())
      {
         assertDependencyNodeEquals(expectedIt.next(), actualIt.next());
      }
   }

   public static void assertDependenciesEquals(List<Dependency> expected, List<Dependency> actual)
   {
      Assert.assertEquals(expected.size(), actual.size());
      final Iterator<Dependency> expectedIt = expected.iterator();
      final Iterator<Dependency> actualIt = actual.iterator();
      while (expectedIt.hasNext())
      {
         assertEquals(expectedIt.next(), actualIt.next());
      }
   }

   public static void assertEquals(Dependency expected, Dependency actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }
      assertNotNull(actual);

      assertArtifactEquals(expected.getArtifact(), actual.getArtifact());
      Assert.assertEquals(expected.getScope(), actual.getScope());
      Assert.assertEquals(expected.getExclusions(), actual.getExclusions());
      Assert.assertEquals(expected.getOptional(), actual.getOptional());
   }

   public static void assertRemoteRepositoriesEquals(List<RemoteRepository> expected, List<RemoteRepository> actual)
   {
      Assert.assertEquals(expected.size(), actual.size());
      final Iterator<RemoteRepository> expectedIt = expected.iterator();
      final Iterator<RemoteRepository> actualIt = actual.iterator();
      while (expectedIt.hasNext())
      {
         assertEquals(expectedIt.next(), actualIt.next());
      }
   }

   public static void assertEquals(RemoteRepository expected, RemoteRepository actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }
      assertNotNull(actual);
      Assert.assertEquals(expected.getContentType(), actual.getContentType());
      Assert.assertEquals(expected.getHost(), actual.getHost());
      Assert.assertEquals(expected.getId(), actual.getId());
      Assert.assertEquals(expected.getProtocol(), actual.getProtocol());
      Assert.assertEquals(expected.getUrl(), actual.getUrl());
      Assert.assertEquals(expected.getAuthentication(), actual.getAuthentication());
      assertRemoteRepositoriesEquals(expected.getMirroredRepositories(), actual.getMirroredRepositories());
      Assert.assertEquals(expected.getProxy(), actual.getProxy());
   }

   public static void assertArtifactsEquals(Collection<Artifact> expected, Collection<Artifact> actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }
      assertNotNull(actual);

      if (expected instanceof Set)
      {
         assertArtifactsEquals(expected, actual);
      }
      else if (expected instanceof List)
      {
         assertTrue(actual instanceof List);
         Assert.assertEquals(expected.size(), actual.size());
         final Iterator<Artifact> expectedIt = expected.iterator();
         final Iterator<Artifact> actualIt = actual.iterator();
         while (expectedIt.hasNext())
         {
            assertArtifactEquals(expectedIt.next(), actualIt.next());
         }
      }
      else
      {
         throw new IllegalArgumentException();
      }
   }

   public static void assertArtifactEquals(Artifact expected, Artifact actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }
      assertNotNull(actual);
      Assert.assertEquals(expected.getGroupId(), actual.getGroupId());
      Assert.assertEquals(expected.getArtifactId(), actual.getArtifactId());
      Assert.assertEquals(expected.getVersion(), actual.getVersion());
      Assert.assertEquals(expected.getExtension(), actual.getExtension());
      Assert.assertEquals(expected.getClassifier(), actual.getClassifier());
      Assert.assertEquals(expected.getFile(), actual.getFile());
      Assert.assertEquals(expected.getProperties(), actual.getProperties());
   }

   public static org.apache.maven.model.Dependency addDependency(Model from, Model to)
   {
      final org.apache.maven.model.Dependency dep = new org.apache.maven.model.Dependency();
      dep.setGroupId(to.getGroupId());
      dep.setArtifactId(to.getArtifactId());
      dep.setVersion(to.getVersion());
      dep.setType(to.getPackaging());
      from.addDependency(dep);
      return dep;
   }

   public static Relocation setRelocation(Model from, Model to)
   {
      final Relocation relocation = new Relocation();
      from.setDistributionManagement(new DistributionManagement());
      from.getDistributionManagement().setRelocation(relocation);
      from.getDistributionManagement().getRelocation().setGroupId(to.getGroupId());
      from.getDistributionManagement().getRelocation().setArtifactId(to.getArtifactId());
      from.getDistributionManagement().getRelocation().setVersion(to.getVersion());
      return relocation;
   }

   public static Model newPom(String id)
   {
      return newPom(id, "1");
   }

   public static Model newPom(String id, String version)
   {
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId(id);
      pom.setArtifactId(id);
      pom.setVersion(version);
      return pom;
   }

   public static Dependency toDependency(ArtifactFactory artifactFactory, Model pom)
   {
      final ArtifactKey key = new ArtifactKey(pom.getGroupId(), pom.getArtifactId(), pom.getVersion(),
         pom.getPackaging(), null);
      final Artifact artifact = artifactFactory.createArtifact(key);
      return new Dependency(artifact, "compile");
   }

   public static Dependency toSystemDependency(ArtifactFactory artifactFactory, Model pom, String localPath)
   {
      final ArtifactKey key = new ArtifactKey(pom.getGroupId(), pom.getArtifactId(), pom.getVersion(),
         pom.getPackaging(), null);
      final Artifact artifact = artifactFactory.createArtifact(key, localPath);
      return new Dependency(artifact, "system");
   }
}
