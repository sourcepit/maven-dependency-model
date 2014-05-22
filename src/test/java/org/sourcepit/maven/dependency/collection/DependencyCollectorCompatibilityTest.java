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
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
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

public class DependencyCollectorCompatibilityTest extends EmbeddedMavenEnvironmentTest
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

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(pom);
         request.addDependency(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(pom);
         request.addDependency(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
   public void testDependency() throws DependencyCollectionException
   {
      final Model a = newPom("a");

      repositoryFacade.deploy(a);

      final HookedRepositorySystemSession mavenSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult maven;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
      assertSession(mavenSession, srcpitSession);
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
            request.setRoot(toSystemDependency(a, ""));
         }
         else
         {
            request.setRoot(toDependency(a).setScope(scope));
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
            request.setRoot(toSystemDependency(a, ""));
         }
         else
         {
            request.setRoot(toDependency(a).setScope(scope));
         }
   
         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }
   
      assertEquals(maven.getRoot(), srcpit.getRoot());
      
      // Inconsistency in aether. Never traverse system scope artifacts  
      srcpitSession.getTraverseDependencyCalls().add(toSystemDependency(a, ""));
      
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
         request.setRoot(toDependency(a));
   
         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }
   
      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));
   
         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }
   
      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());

      // DefaultDependencyCollector doesn't call DependencyTraverser for a. Compared with
      // testDependency_RelocationDepth1 I think this is an inconsistency in aether
      mavenSession.getTraverseDependencyCalls().add(0, toDependency(a));

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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());

      // DefaultDependencyCollector doesn't call DependencyTraverser for a. Compared with
      // testDependency_RelocationDepth1 I think this is an inconsistency in aether
      mavenSession.getTraverseDependencyCalls().add(0, toDependency(a));

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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(pom).setOptional(Boolean.TRUE));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(pom).setOptional(Boolean.TRUE));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));
         request.addDependency(toDependency(c));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));
         request.addDependency(toDependency(c));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a1));
         request.addManagedDependency(toDependency(a2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a1));
         request.addManagedDependency(toDependency(a2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));
         request.addManagedDependency(toDependency(b2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));
         request.addManagedDependency(toDependency(b2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));
         request.addManagedDependency(toDependency(b2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));
         request.addManagedDependency(toDependency(b2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
            request.addDependency(toSystemDependency(a, ""));
         }
         else
         {
            request.addDependency(toDependency(a).setScope(scope));
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
            request.addDependency(toSystemDependency(a, ""));
         }
         else
         {
            request.addDependency(toDependency(a).setScope(scope));
         }

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a1));
         request.addDependency(toDependency(a2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a1));
         request.addDependency(toDependency(a2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a2));
         request.addDependency(toDependency(a1));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a2));
         request.addDependency(toDependency(a1));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a).setOptional(Boolean.TRUE));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a).setOptional(Boolean.TRUE));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         Dependency dependency = toDependency(a1);
         request.addDependency(dependency);
         request.addManagedDependency(toDependency(a2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(a1);
         request.addDependency(dependency);
         request.addManagedDependency(toDependency(a2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);
         request.addManagedDependency(toDependency(b2));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         Dependency dependency = toDependency(a);
         request.addDependency(dependency);
         request.addManagedDependency(toDependency(b2));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.setRoot(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.setRoot(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());

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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());
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
         request.addDependency(toDependency(a));

         maven = defaultDependencyCollector.collectDependencies(mavenSession, request);
         System.out.println(TestHarness.toString(maven));
      }

      final HookedRepositorySystemSession srcpitSession = new HookedRepositorySystemSession(
         buildContext.getRepositorySession());
      final CollectResult srcpit;
      {
         CollectRequest request = newCollectRequest();
         request.addDependency(toDependency(a));

         srcpit = srcpitDependencyCollector.collectDependencies(srcpitSession, request);
         System.out.println(TestHarness.toString(srcpit));
      }

      assertEquals(maven.getRoot(), srcpit.getRoot());

      // we are more precise then aether and call selectDependency for dependencies before resolving the artifact
      // descriptor and for relocations after resolving
      Dependency _c1 = srcpitSession.getSelectDependencyCalls().remove(2);
      srcpitSession.getSelectDependencyCalls().add(_c1);

      assertSession(mavenSession, srcpitSession);
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

   private static Relocation setRelocation(Model from, Model to)
   {
      final Relocation relocation = new Relocation();
      from.setDistributionManagement(new DistributionManagement());
      from.getDistributionManagement().setRelocation(relocation);
      from.getDistributionManagement().getRelocation().setGroupId(to.getGroupId());
      from.getDistributionManagement().getRelocation().setArtifactId(to.getArtifactId());
      from.getDistributionManagement().getRelocation().setVersion(to.getVersion());
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
      final ArtifactKey key = new ArtifactKey(pom.getGroupId(), pom.getArtifactId(), pom.getVersion(),
         pom.getPackaging(), null);
      final Artifact artifact = artifactFactory.createArtifact(key);
      return new Dependency(artifact, "compile");
   }

   private Dependency toSystemDependency(final Model pom, String localPath)
   {
      final ArtifactKey key = new ArtifactKey(pom.getGroupId(), pom.getArtifactId(), pom.getVersion(),
         pom.getPackaging(), null);
      final Artifact artifact = artifactFactory.createArtifact(key, localPath);
      return new Dependency(artifact, "system");
   }

}
