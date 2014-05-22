/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Relocation;
import org.apache.maven.plugin.LegacySupport;
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.sourcepit.common.maven.artifact.ArtifactFactory;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.maven.dependency.model.poc.TestHarness;

public class SrcpitDependencyCollectorTest extends EmbeddedMavenEnvironmentTest
{
   @Inject
   private ArtifactRepositoryFacade repositoryFacade;

   @Inject
   private LegacySupport buildContext;

   @Inject
   private SrcpitDependencyCollector srcpitDependencyCollector;

   @Inject
   private DependencyCollector defaultDependencyCollector;

   @Inject
   private ArtifactFactory artifactFactory;

   @Rule
   public TestName name = new TestName();

   @Override
   protected Environment newEnvironment()
   {
      return Environment.get("env-test.properties");
   }

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      repositoryFacade.setEmbeddedMaven(getEmbeddedMaven());

      final MavenSession session = buildStubProject(getLocalRepositoryPath()).getSession();
      buildContext.setSession(session);
   }

   @Override
   @After
   public void tearDown() throws Exception
   {
      buildContext.setSession(null);
      super.tearDown();
   }

   @Override
   protected File getLocalRepositoryPath()
   {
      return getWs().newDir("local-repo");
   }

   protected File getRemoteRepositoryPath()
   {
      return getWs().newDir("remote-repo");
   }

   protected String getTestName()
   {
      return name.getMethodName().substring("test".length());
   }

   @Test
   public void testDependencies_Single() throws DependencyCollectionException
   {
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId("a");
      pom.setArtifactId("a");
      pom.setVersion("1");

      repositoryFacade.deploy(pom);

      final HookedSession mavenSession = new HookedSession(buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(pom);
         request.addDependency(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedSession srcpitSession = new HookedSession(buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(pom);
         request.addDependency(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
      assertDependenciesEquals(mavenSession.getSelectDependencyCalls(), srcpitSession.getSelectDependencyCalls());
   }
   
   @Test
   public void testDependencies_Transitive() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      addDependency(a, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedSession mavenSession = new HookedSession(buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedSession srcpitSession = new HookedSession(buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
      assertDependenciesEquals(mavenSession.getSelectDependencyCalls(), srcpitSession.getSelectDependencyCalls());
   }

   @Test
   public void testDependency_Single() throws DependencyCollectionException
   {
      final Model pom = newPom("a");
      
      repositoryFacade.deploy(pom);

      final HookedSession mavenSession = new HookedSession(buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(pom);
         request.setRoot(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedSession srcpitSession = new HookedSession(buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(pom);
         request.setRoot(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
      assertDependenciesEquals(mavenSession.getSelectDependencyCalls(), srcpitSession.getSelectDependencyCalls());
   }
   
   @Test
   public void testDependency_WithExtraDependency() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      repositoryFacade.deploy(a);
      
      final Model b = newPom("b");
      repositoryFacade.deploy(b);

      final HookedSession mavenSession = new HookedSession(buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));
         request.addDependency(toDependency(b));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedSession srcpitSession = new HookedSession(buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));
         request.addDependency(toDependency(b));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
      assertDependenciesEquals(mavenSession.getSelectDependencyCalls(), srcpitSession.getSelectDependencyCalls());
   }

   private static final class HookedSession extends AbstractForwardingRepositorySystemSession
   {
      private final List<Dependency> selectDependencyCalls = new ArrayList<Dependency>();

      private final RepositorySystemSession session;

      public HookedSession(RepositorySystemSession session)
      {
         this.session = session;
      }

      public List<Dependency> getSelectDependencyCalls()
      {
         return selectDependencyCalls;
      }

      @Override
      protected RepositorySystemSession getSession()
      {
         return session;
      }

      @Override
      public DependencySelector getDependencySelector()
      {
         return new DependencySelectorRecorder(getSession().getDependencySelector(), selectDependencyCalls);
      }
   }

   private static class DependencySelectorRecorder implements DependencySelector
   {
      private final DependencySelector dependencySelector;
      private final List<Dependency> selectDependencyCalls;

      public DependencySelectorRecorder(DependencySelector dependencySelector, List<Dependency> selectDependencyCalls)
      {
         this.dependencySelector = dependencySelector;
         this.selectDependencyCalls = selectDependencyCalls;
      }

      @Override
      public boolean selectDependency(Dependency dependency)
      {
         selectDependencyCalls.add(dependency);
         return dependencySelector.selectDependency(dependency);
      }

      @Override
      public DependencySelector deriveChildSelector(DependencyCollectionContext context)
      {
         return new DependencySelectorRecorder(dependencySelector.deriveChildSelector(context), selectDependencyCalls);
      }
   }

   @Test
   public void testConflictByRelocation() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      final Model b = newPom("b");
      setRelocation(b, "c").setVersion("2");

      final Model c = newPom("c");

      addDependency(a, c);
      addDependency(a, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);
      repositoryFacade.deploy(newPom("c", "2"));

      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);

         final RepositorySystemSession session = buildContext.getRepositorySession();
         CollectResult result = defaultDependencyCollector.collectDependencies(session, request);
         System.out.println(TestHarness.toString(result));
      }

      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);

         final RepositorySystemSession session = buildContext.getRepositorySession();
         CollectResult result = srcpitDependencyCollector.collectDependencies(session, request);
         System.out.println(TestHarness.toString(result));
      }
   }


   private static void assertEquals(DependencyNode expected, DependencyNode actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }
      assertNotNull(actual);

      Assert.assertEquals(expected.getRequestContext(), actual.getRequestContext());
      assertArtifactsEquals(expected.getAliases(), actual.getAliases());
      assertEquals(expected.getArtifact(), actual.getArtifact());
      assertEquals(expected.getDependency(), expected.getDependency());
      Assert.assertEquals(expected.getManagedBits(), actual.getManagedBits());
      assertArtifactsEquals(expected.getRelocations(), actual.getRelocations());
      assertRemoteRepositoriesEquals(expected.getRepositories(), actual.getRepositories());
      Assert.assertEquals(expected.getVersion(), actual.getVersion());
      Assert.assertEquals(expected.getVersionConstraint(), actual.getVersionConstraint());

      assertDependencyNodesEquals(expected.getChildren(), actual.getChildren());
   }

   private static void assertDependencyNodesEquals(List<DependencyNode> expected, List<DependencyNode> actual)
   {
      Assert.assertEquals(expected.size(), actual.size());
      final Iterator<DependencyNode> expectedIt = expected.iterator();
      final Iterator<DependencyNode> actualIt = actual.iterator();
      while (expectedIt.hasNext())
      {
         assertEquals(expectedIt.next(), actualIt.next());
      }
   }

   private static void assertDependenciesEquals(List<Dependency> expected, List<Dependency> actual)
   {
      Assert.assertEquals(expected.size(), actual.size());
      final Iterator<Dependency> expectedIt = expected.iterator();
      final Iterator<Dependency> actualIt = actual.iterator();
      while (expectedIt.hasNext())
      {
         assertEquals(expectedIt.next(), actualIt.next());
      }
   }

   private static void assertEquals(Dependency expected, Dependency actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }
      assertNotNull(actual);

      assertEquals(expected.getArtifact(), actual.getArtifact());
      Assert.assertEquals(expected.getScope(), actual.getScope());
      Assert.assertEquals(expected.getExclusions(), actual.getExclusions());
      Assert.assertEquals(expected.getOptional(), actual.getOptional());
   }

   private static void assertRemoteRepositoriesEquals(List<RemoteRepository> expected, List<RemoteRepository> actual)
   {
      Assert.assertEquals(expected.size(), actual.size());
      final Iterator<RemoteRepository> expectedIt = expected.iterator();
      final Iterator<RemoteRepository> actualIt = actual.iterator();
      while (expectedIt.hasNext())
      {
         assertEquals(expectedIt.next(), actualIt.next());
      }
   }

   private static void assertEquals(RemoteRepository expected, RemoteRepository actual)
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

   private static void assertArtifactsEquals(Collection<Artifact> expected, Collection<Artifact> actual)
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
            assertEquals(expectedIt.next(), actualIt.next());
         }
      }
      else
      {
         throw new IllegalArgumentException();
      }
   }

   private static void assertEquals(Artifact expected, Artifact actual)
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

   private static org.apache.maven.model.Dependency addDependency(Model from, Model to)
   {
      final org.apache.maven.model.Dependency dep = new org.apache.maven.model.Dependency();
      dep.setGroupId(to.getGroupId());
      dep.setArtifactId(to.getArtifactId());
      dep.setVersion(to.getVersion());
      dep.setType(to.getPackaging());
      from.addDependency(dep);
      return dep;
   }

   @Test
   public void testRelocation() throws DependencyCollectionException
   {
      final Model pom = newPom("a");

      String id = "b";

      setRelocation(pom, id);

      repositoryFacade.deploy(pom);
      repositoryFacade.deploy(newPom(id));

      final RepositorySystemSession session = buildContext.getRepositorySession();

      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(pom);
         request.addDependency(dependency);

         CollectResult result = srcpitDependencyCollector.collectDependencies(session, request);
         System.out.println(TestHarness.toString(result));
      }

      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(pom);
         request.addDependency(dependency);

         CollectResult result = defaultDependencyCollector.collectDependencies(session, request);
         System.out.println(TestHarness.toString(result));
      }
   }

   private static Relocation setRelocation(final Model pom, String id)
   {
      final Relocation relocation = new Relocation();
      pom.setDistributionManagement(new DistributionManagement());
      pom.getDistributionManagement().setRelocation(relocation);
      pom.getDistributionManagement().getRelocation().setGroupId(id);
      pom.getDistributionManagement().getRelocation().setArtifactId(id);
      pom.getDistributionManagement().getRelocation().setVersion("1");
      return relocation;
   }

   private Model newPom(String id)
   {
      return newPom(id, "1");
   }

   private Model newPom(String id, String version)
   {
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId(id);
      pom.setArtifactId(id);
      pom.setVersion(version);
      return pom;
   }

   private CollectRequest newCollectRequest()
   {
      CollectRequest request = new CollectRequest();
      request.addRepository(RepositoryUtils.toRepo(embeddedMaven.getRemoteRepository()));
      return request;
   }

   private Dependency toDependency(final Model pom)
   {
      Artifact artifact = artifactFactory.createArtifact(new ArtifactKey(pom.getGroupId(), pom.getArtifactId(), pom
         .getVersion(), pom.getPackaging(), null));
      Dependency dependency = new Dependency(artifact, "compile");
      return dependency;
   }

}
