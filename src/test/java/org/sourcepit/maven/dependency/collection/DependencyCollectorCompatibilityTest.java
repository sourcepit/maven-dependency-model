/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.addDependency;
import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.assertDependenciesEquals;
import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.assertDependencyNodeEquals;
import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.newPom;
import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.setRelocation;
import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.toDependency;
import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.toSystemDependency;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.superpom.SuperPomProvider;
import org.apache.maven.plugin.LegacySupport;
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.resolution.ArtifactDescriptorPolicy;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.sourcepit.common.maven.artifact.ArtifactFactory;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.maven.dependency.model.poc.TestHarness;

import com.google.inject.Binder;

public class DependencyCollectorCompatibilityTest extends EmbeddedMavenEnvironmentTest
{
   private final static class RepositorySystemSessionImpl extends AbstractForwardingRepositorySystemSession
   {
      private final RepositorySystemSession session;

      private ArtifactDescriptorPolicy artifactDescriptorPolicy;

      private RepositorySystemSessionImpl(RepositorySystemSession session)
      {
         this.session = session;
      }

      @Override
      protected RepositorySystemSession getSession()
      {
         return session;
      }

      public void setArtifactDescriptorPolicy(ArtifactDescriptorPolicy artifactDescriptorPolicy)
      {
         this.artifactDescriptorPolicy = artifactDescriptorPolicy;
      }

      @Override
      public ArtifactDescriptorPolicy getArtifactDescriptorPolicy()
      {
         return artifactDescriptorPolicy == null ? super.getArtifactDescriptorPolicy() : artifactDescriptorPolicy;
      }
   }

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
   protected void configureCustomBindings(Binder binder)
   {
      super.configureCustomBindings(binder);
      // avoid requests to central (that will fail on system behind proxies and without permission to get out)
      binder.bind(SuperPomProvider.class).to(FilterCentralFromSuperPom.class);
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

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, pom);
         request.addDependency(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, pom);
         request.addDependency(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   private void assertSession(final HookedRepositorySystemSession mavenSession,
      final HookedRepositorySystemSession srcpitSession)
   {
      Assert.assertEquals(mavenSession.getDeriveChildSelectorCalls(), srcpitSession.getDeriveChildSelectorCalls());
      assertDependenciesEquals(mavenSession.getSelectDependencyCalls(), srcpitSession.getSelectDependencyCalls());

      Assert.assertEquals(mavenSession.getDeriveChildManagerCalls(), srcpitSession.getDeriveChildManagerCalls());
      assertDependenciesEquals(mavenSession.getManageDependencyCalls(), srcpitSession.getManageDependencyCalls());

      Assert.assertEquals(mavenSession.getDeriveChildTraverserCalls(), srcpitSession.getDeriveChildTraverserCalls());
      assertDependenciesEquals(mavenSession.getTraverseDependencyCalls(), srcpitSession.getTraverseDependencyCalls());
   }

   @Test
   public void testDependency_Unresolvable() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      testDependency(a, buildContext.getRepositorySession(), false);
   }

   @Test
   public void testDependency_Unresolvable2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b);

      repositoryFacade.deploy(a);

      RepositorySystemSessionImpl session = new RepositorySystemSessionImpl(buildContext.getRepositorySession());
      session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(true, true));
      testDependency(a, session, false);
   }

   @Test
   public void testDependency_Unresolvable3() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b);

      repositoryFacade.deploy(a);

      RepositorySystemSessionImpl session = new RepositorySystemSessionImpl(buildContext.getRepositorySession());
      session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(false, false));
      testDependency(a, session, true);
   }

   private void testDependency(final Model dependency, RepositorySystemSession session, boolean expectEx)
   {
      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(session);
      CollectResult maven = null;
      DependencyCollectionException mavenEx = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, dependency));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));

         if (expectEx)
         {
            fail();
         }
      }
      catch (DependencyCollectionException e)
      {
         mavenEx = e;
         maven = e.getResult();
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(session);
      CollectResult srcpit = null;
      DependencyCollectionException srcpitEx = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, dependency));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));

         if (expectEx)
         {
            fail();
         }
      }
      catch (DependencyCollectionException e)
      {
         srcpitEx = e;
         srcpit = e.getResult();
      }

      Assert.assertEquals(mavenEx == null, srcpitEx == null);
      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
      assertCollectionExceptions(maven.getExceptions(), srcpit.getExceptions());
   }

   private static void assertCollectionExceptions(List<Exception> expected, List<Exception> actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }
      assertNotNull(actual);

      Assert.assertEquals(expected.size(), actual.size());


   }

   @Test
   public void testDependency() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_VersionRangeEx_Depth0() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      Dependency d = toDependency(artifactFactory, a);
      d = d.setArtifact(d.getArtifact().setVersion("[6,9)"));

      CollectResult maven = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(d);

         defaultDependencyCollector.collectDependencies(mavenSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         maven = e.getResult();
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult srcpit = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(d);

         srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         srcpit = e.getResult();
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());

      // inconsistency in aether
      srcpitSession.getTraverseDependencyCalls().clear();

      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_VersionRangeEx_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b).setVersion("[6,9");

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult maven = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         defaultDependencyCollector.collectDependencies(mavenSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         maven = e.getResult();
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult srcpit = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         srcpit = e.getResult();
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_VersionRangeEx_Depth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      addDependency(b, c).setVersion("[6,9");

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult maven = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         defaultDependencyCollector.collectDependencies(mavenSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         maven = e.getResult();
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult srcpit = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         srcpit = e.getResult();
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_Cycle1() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      addDependency(a, a);

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_Cycle2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b);
      addDependency(b, a);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_CyclicRelocation() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b);
      setRelocation(b, a);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_CyclicRelocation2_Depth0() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      setRelocation(a, b);
      setRelocation(b, a);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_CyclicRelocation2_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      setRelocation(b, c);
      setRelocation(c, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      // final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
      // buildContext.getRepositorySession());
      // final CollectResult maven;
      // {
      // CollectRequest request = newCollectRequest();
      // request.setRoot(toDependency(artifactFactory, a));
      //
      // maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
      // System.out.println(TestHarness.toString(maven));
      // }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      // assertEquals(maven.getRoot(), srcpit.getRoot());
      // assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ScopeTest_Depth0() throws DependencyCollectionException
   {
      testDependency_Scope_Depth0("test");
   }

   @Test
   public void testDependency_ScopeTest_Depth1() throws DependencyCollectionException
   {
      testDependency_Scope_Depth1("test");
   }

   @Test
   public void testDependency_ScopeTest_Depth2() throws DependencyCollectionException
   {
      testDependency_Scope_Depth2("test");
   }

   @Test
   public void testDependency_ScopeSystem_Depth0() throws DependencyCollectionException
   {
      testDependency_Scope_Depth0("system");
   }

   @Test
   public void testDependency_ScopeSystem_Depth1() throws DependencyCollectionException
   {
      testDependency_Scope_Depth1("system");
   }

   @Test
   public void testDependency_ScopeSystem_Depth2() throws DependencyCollectionException
   {
      testDependency_Scope_Depth2("system");
   }

   @Test
   public void testDependency_ScopeProvided_Depth0() throws DependencyCollectionException
   {
      testDependency_Scope_Depth0("provided");
   }

   @Test
   public void testDependency_ScopeProvided_Depth1() throws DependencyCollectionException
   {
      testDependency_Scope_Depth1("provided");
   }

   @Test
   public void testDependency_ScopeProvided_Depth2() throws DependencyCollectionException
   {
      testDependency_Scope_Depth2("provided");
   }

   @Test
   public void testDependency_ScopeRuntime_Depth0() throws DependencyCollectionException
   {
      testDependency_Scope_Depth0("runtime");
   }

   @Test
   public void testDependency_ScopeRuntime_Depth1() throws DependencyCollectionException
   {
      testDependency_Scope_Depth1("runtime");
   }

   @Test
   public void testDependency_ScopeRuntime_Depth2() throws DependencyCollectionException
   {
      testDependency_Scope_Depth2("runtime");
   }

   private void testDependency_Scope_Depth0(final String scope) throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      addDependency(b, c);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         if ("system".equals(scope))
         {
            request.setRoot(toSystemDependency(artifactFactory, a, ""));
         }
         else
         {
            request.setRoot(toDependency(artifactFactory, a).setScope(scope));
         }

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         if ("system".equals(scope))
         {
            request.setRoot(toSystemDependency(artifactFactory, a, ""));
         }
         else
         {
            request.setRoot(toDependency(artifactFactory, a).setScope(scope));
         }

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());

      // Inconsistency in aether. Never traverse system scope artifacts
      if ("system".equals(scope))
      {
         srcpitSession.getTraverseDependencyCalls().add(toSystemDependency(artifactFactory, a, ""));
      }

      assertSession(mavenSession, srcpitSession);
   }

   private void testDependency_Scope_Depth1(final String scope) throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      org.apache.maven.model.Dependency dep = addDependency(a, b);
      dep.setScope(scope);
      if ("system".equals(scope))
      {
         dep.setSystemPath("");
      }

      addDependency(b, c);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   private void testDependency_Scope_Depth2(final String scope) throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);

      org.apache.maven.model.Dependency dep = addDependency(b, c);
      dep.setScope(scope);
      if ("system".equals(scope))
      {
         dep.setSystemPath("");
      }

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ConflictingSiblings1_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b1 = newPom("b", "1");
      final Model b2 = newPom("b", "2");

      addDependency(a, b1);
      addDependency(a, b2);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ConflictingSiblings1_Depth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c1 = newPom("c", "1");
      final Model c2 = newPom("c", "2");

      addDependency(a, b);
      addDependency(b, c1);
      addDependency(b, c2);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c1);
      repositoryFacade.deploy(c2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ConflictingSiblings2_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b1 = newPom("b", "1");
      final Model b2 = newPom("b", "2");

      addDependency(a, b2);
      addDependency(a, b1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ConflictingSiblings2_Depth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c1 = newPom("c", "1");
      final Model c2 = newPom("c", "2");

      addDependency(a, b);
      addDependency(b, c2);
      addDependency(b, c1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c1);
      repositoryFacade.deploy(c2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_RelocationDepth0() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      setRelocation(a, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());

      // DefaultDependencyCollector doesn't call DependencyTraverser for a. Compared with
      // testDependency_RelocationDepth1 I think this is an inconsistency in aether
      mavenSession.getTraverseDependencyCalls().add(0, toDependency(artifactFactory, a));

      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_RelocationDepth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      setRelocation(b, c);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_RelocationDepth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");
      final Model d = newPom("d");

      addDependency(a, b);
      addDependency(b, c);
      setRelocation(c, d);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);
      repositoryFacade.deploy(d);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_RelocationChainDepth0() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      setRelocation(a, b);
      setRelocation(b, c);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());

      // DefaultDependencyCollector doesn't call DependencyTraverser for a. Compared with
      // testDependency_RelocationDepth1 I think this is an inconsistency in aether
      mavenSession.getTraverseDependencyCalls().add(0, toDependency(artifactFactory, a));

      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_RelocationChainDepth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");
      final Model d = newPom("d");

      addDependency(a, b);
      setRelocation(b, c);
      setRelocation(c, d);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);
      repositoryFacade.deploy(d);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_OptionalDepth0() throws DependencyCollectionException
   {
      final Model pom = newPom("a");

      repositoryFacade.deploy(pom);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, pom).setOptional(Boolean.TRUE));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, pom).setOptional(Boolean.TRUE));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_OptionalDepth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b).setOptional(true);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_OptionalDepth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      addDependency(b, c).setOptional(true);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }


   @Test
   public void testDependency_Transitive() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_WithExtraDependency() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));
         request.addDependency(toDependency(artifactFactory, c));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));
         request.addDependency(toDependency(artifactFactory, c));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ManagedDepth0() throws DependencyCollectionException
   {
      final Model a1 = newPom("a", "1");
      final Model a2 = newPom("a", "2");

      repositoryFacade.deploy(a1);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a1));
         request.addManagedDependency(toDependency(artifactFactory, a2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a1));
         request.addManagedDependency(toDependency(artifactFactory, a2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ManagedDepth1() throws DependencyCollectionException
   {
      final Model a = newPom("a", "1");
      final Model b1 = newPom("b", "1");
      final Model b2 = newPom("b", "2");

      addDependency(a, b1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));
         request.addManagedDependency(toDependency(artifactFactory, b2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));
         request.addManagedDependency(toDependency(artifactFactory, b2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ManagedDepth2() throws DependencyCollectionException
   {
      final Model a = newPom("a", "1");
      final Model c = newPom("c", "1");
      final Model b1 = newPom("b", "1");
      final Model b2 = newPom("b", "2");

      addDependency(a, c);
      addDependency(c, b1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(c);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));
         request.addManagedDependency(toDependency(artifactFactory, b2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));
         request.addManagedDependency(toDependency(artifactFactory, b2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, a);
         request.addDependency(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, a);
         request.addDependency(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_UnambiguousRange() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b1 = newPom("b", "1.1");
      final Model b2 = newPom("b", "1.2");

      addDependency(a, b1).setVersion("[1,2)");

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testRootArtifact() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRootArtifact(toDependency(artifactFactory, a).getArtifact());

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRootArtifact(toDependency(artifactFactory, a).getArtifact());

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testRootArtifact_WithDependencies() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      addDependency(b, c);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRootArtifact(toDependency(artifactFactory, a).getArtifact());
         request.addDependency(toDependency(artifactFactory, b));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRootArtifact(toDependency(artifactFactory, a).getArtifact());
         request.addDependency(toDependency(artifactFactory, b));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testRootArtifact_CycleWithRootArtifact() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      addDependency(b, c);
      addDependency(c, a);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRootArtifact(toDependency(artifactFactory, a).getArtifact());
         request.addDependency(toDependency(artifactFactory, b));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRootArtifact(toDependency(artifactFactory, a).getArtifact());
         request.addDependency(toDependency(artifactFactory, b));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_VersionRangeEx_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b).setVersion("[6,9");

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult maven = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         defaultDependencyCollector.collectDependencies(mavenSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         maven = e.getResult();
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult srcpit = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         srcpit = e.getResult();
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_VersionRangeEx_Depth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      addDependency(b, c).setVersion("[6,9");

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult maven = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         defaultDependencyCollector.collectDependencies(mavenSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         maven = e.getResult();
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());

      CollectResult srcpit = null;
      try
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         fail();
      }
      catch (DependencyCollectionException e)
      {
         assertTrue(e.getCause() instanceof VersionRangeResolutionException);
         srcpit = e.getResult();
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_Cycle1() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      addDependency(a, a);

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_Cycle2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b);
      addDependency(b, a);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_CyclicRelocation() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b);
      setRelocation(b, a);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ScopeTest_Depth1() throws DependencyCollectionException
   {
      testDependencies_Scope_Depth1("test");
   }

   @Test
   public void testDependencies_ScopeTest_Depth2() throws DependencyCollectionException
   {
      testDependencies_Scope_Depth2("test");
   }

   @Test
   public void testDependencies_ScopeSystem_Depth1() throws DependencyCollectionException
   {
      testDependencies_Scope_Depth1("system");
   }

   @Test
   public void testDependencies_ScopeSystem_Depth2() throws DependencyCollectionException
   {
      testDependencies_Scope_Depth2("system");
   }

   @Test
   public void testDependencies_ScopeRuntime_Depth1() throws DependencyCollectionException
   {
      testDependencies_Scope_Depth1("runtime");
   }

   @Test
   public void testDependencies_ScopeRuntime_Depth2() throws DependencyCollectionException
   {
      testDependencies_Scope_Depth2("runtime");
   }

   @Test
   public void testDependencies_ScopeProvided_Depth1() throws DependencyCollectionException
   {
      testDependencies_Scope_Depth1("provided");
   }

   @Test
   public void testDependencies_ScopeProvided_Depth2() throws DependencyCollectionException
   {
      testDependencies_Scope_Depth2("provided");
   }

   private void testDependencies_Scope_Depth1(final String scope) throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();

         if ("system".equals(scope))
         {
            request.addDependency(toSystemDependency(artifactFactory, a, ""));
         }
         else
         {
            request.addDependency(toDependency(artifactFactory, a).setScope(scope));
         }

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();

         if ("system".equals(scope))
         {
            request.addDependency(toSystemDependency(artifactFactory, a, ""));
         }
         else
         {
            request.addDependency(toDependency(artifactFactory, a).setScope(scope));
         }

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   private void testDependencies_Scope_Depth2(final String scope) throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      org.apache.maven.model.Dependency dep = addDependency(a, b);
      dep.setScope(scope);
      if ("system".equals(scope))
      {
         dep.setSystemPath("");
      }

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ConflictingSiblings1_Depth1() throws DependencyCollectionException
   {
      final Model a1 = newPom("a", "1");
      final Model a2 = newPom("a", "2");

      repositoryFacade.deploy(a1);
      repositoryFacade.deploy(a2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a1));
         request.addDependency(toDependency(artifactFactory, a2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a1));
         request.addDependency(toDependency(artifactFactory, a2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ConflictingSiblings1_Depth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b1 = newPom("b", "1");
      final Model b2 = newPom("b", "2");

      addDependency(a, b1);
      addDependency(a, b2);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ConflictingSiblings2_Depth1() throws DependencyCollectionException
   {
      final Model a1 = newPom("a", "1");
      final Model a2 = newPom("a", "2");

      repositoryFacade.deploy(a1);
      repositoryFacade.deploy(a2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a2));
         request.addDependency(toDependency(artifactFactory, a1));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a2));
         request.addDependency(toDependency(artifactFactory, a1));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ConflictingSiblings2_Depth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b1 = newPom("b", "1");
      final Model b2 = newPom("b", "2");

      addDependency(a, b2);
      addDependency(a, b1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_RelocationDepth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      setRelocation(a, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_RelocationDepth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      addDependency(a, b);
      setRelocation(b, c);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ManagedRelocation() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b1 = newPom("b", "1");
      final Model b2 = newPom("b", "2");

      addDependency(a, b1);
      setRelocation(b1, b2);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));
         request.addManagedDependency(toDependency(artifactFactory, b1));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));
         request.addManagedDependency(toDependency(artifactFactory, b1));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ManagedRelocation2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b1 = newPom("b", "1");
      final Model c1 = newPom("c", "1");
      final Model c2 = newPom("c", "2");

      addDependency(a, b1);
      setRelocation(b1, c1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(c1);
      repositoryFacade.deploy(c2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));
         request.addManagedDependency(toDependency(artifactFactory, c2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));
         request.addManagedDependency(toDependency(artifactFactory, c2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_RelocationChainDepth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");

      setRelocation(a, b);
      setRelocation(b, c);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_RelocationChainDepth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c = newPom("c");
      final Model d = newPom("d");

      addDependency(a, b);
      setRelocation(b, c);
      setRelocation(c, d);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c);
      repositoryFacade.deploy(d);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_OptionalDepth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a).setOptional(Boolean.TRUE));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a).setOptional(Boolean.TRUE));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_OptionalDepth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");

      addDependency(a, b).setOptional(true);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_Transitive() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      addDependency(a, b);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, a);
         request.addDependency(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, a);
         request.addDependency(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ManagedDepth1() throws DependencyCollectionException
   {
      final Model a1 = newPom("a", "1");
      final Model a2 = newPom("a", "2");

      repositoryFacade.deploy(a1);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, a1);
         request.addDependency(dependency);
         request.addManagedDependency(toDependency(artifactFactory, a2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, a1);
         request.addDependency(dependency);
         request.addManagedDependency(toDependency(artifactFactory, a2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ManagedDepth2() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b1 = newPom("b", "1");
      final Model b2 = newPom("b", "2");
      addDependency(a, b1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b1);
      repositoryFacade.deploy(b2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, a);
         request.addDependency(dependency);
         request.addManagedDependency(toDependency(artifactFactory, b2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(artifactFactory, a);
         request.addDependency(dependency);
         request.addManagedDependency(toDependency(artifactFactory, b2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ConflictByRelocation1_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c1 = newPom("c", "1");
      final Model c2 = newPom("c", "2");

      addDependency(a, c1);

      addDependency(a, b);
      setRelocation(b, c2);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c1);
      repositoryFacade.deploy(c2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependency_ConflictByRelocation2_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c1 = newPom("c", "1");
      final Model c2 = newPom("c", "2");

      addDependency(a, b);
      setRelocation(b, c2);

      addDependency(a, c1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c1);
      repositoryFacade.deploy(c2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());

      // we are more precise then aether and call selectDependency for dependencies before resolving the artifact
      // descriptor and for relocations after resolving
      Dependency d = srcpitSession.getSelectDependencyCalls().remove(1);
      srcpitSession.getSelectDependencyCalls().add(d);

      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ConflictByRelocation1_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c1 = newPom("c", "1");
      final Model c2 = newPom("c", "2");

      addDependency(a, c1);

      addDependency(a, b);
      setRelocation(b, c2);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c1);
      repositoryFacade.deploy(c2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
   }

   @Test
   public void testDependencies_ConflictByRelocation2_Depth1() throws DependencyCollectionException
   {
      final Model a = newPom("a");
      final Model b = newPom("b");
      final Model c1 = newPom("c", "1");
      final Model c2 = newPom("c", "2");

      addDependency(a, b);
      setRelocation(b, c2);

      addDependency(a, c1);

      repositoryFacade.deploy(a);
      repositoryFacade.deploy(b);
      repositoryFacade.deploy(c1);
      repositoryFacade.deploy(c2);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(artifactFactory, a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertDependencyNodeEquals(maven.getRoot(), srcpit.getRoot());

      // we are more precise then aether and call selectDependency for dependencies before resolving the artifact
      // descriptor and for relocations after resolving
      Dependency _c1 = srcpitSession.getSelectDependencyCalls().remove(2);
      srcpitSession.getSelectDependencyCalls().add(_c1);

      assertSession(mavenSession, srcpitSession);
   }

   private CollectRequest newCollectRequest()
   {
      CollectRequest request = new CollectRequest();
      request.addRepository(RepositoryUtils.toRepo(embeddedMaven.getRemoteRepository()));
      return request;
   }

}
