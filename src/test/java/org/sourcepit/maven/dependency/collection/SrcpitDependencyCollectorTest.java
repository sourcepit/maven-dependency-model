/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.io.File;

import javax.inject.Inject;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Relocation;
import org.apache.maven.plugin.LegacySupport;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DependencyCollector;
import org.junit.After;
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
   public void test() throws DependencyCollectionException
   {
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId("a");
      pom.setArtifactId("a");
      pom.setVersion("1");

      repositoryFacade.deploy(pom);

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
      
      CollectRequest request = newCollectRequest();
      Dependency dependency = toDependency(a);
      request.addDependency(dependency);
      
      final RepositorySystemSession session = buildContext.getRepositorySession();
      CollectResult result = defaultDependencyCollector.collectDependencies(session, request);
      System.out.println(TestHarness.toString(result));
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
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId(id);
      pom.setArtifactId(id);
      pom.setVersion("1");
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
