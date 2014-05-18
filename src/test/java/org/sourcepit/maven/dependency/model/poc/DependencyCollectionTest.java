/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.ModelWriter;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.util.graph.transformer.NoopDependencyGraphTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;

public class DependencyCollectionTest extends EmbeddedMavenEnvironmentTest
{
   @Inject
   private ArtifactRepositoryFacade repositoryFacade;

   @Inject
   private LegacySupport buildContext;

   @Inject
   private ModelWriter modelWriter;

   @Inject
   private DependencyCollector dependencyCollector;

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
   public void testOptional_01() throws Exception
   {
      test();
   }

   @Test
   public void testOptional_02() throws Exception
   {
      test();
   }

   @Test
   public void testOptional_03() throws Exception
   {
      test();
   }

   @Test
   public void testOptional_04() throws Exception
   {
      test();
   }

   @Test
   public void testOptional_05() throws Exception
   {
      test();
   }

   @Test
   public void testOptional_06() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_01_Test() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_02_Test() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_03_Test() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_04_Test() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_05_Test() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_06_Test() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_01_System() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_02_System() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_03_System() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_04_System() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_05_System() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_06_System() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_01_Provided() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_02_Provided() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_03_Provided() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_04_Provided() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_05_Provided() throws Exception
   {
      test();
   }

   @Test
   public void testScopes_06_Provided() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict_01() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict_02() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict_03() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict_04() throws Exception
   {
      test();
   }
   
   @Test
   public void testVersionConflict_05() throws Exception
   {
      test();
   }

   @Test
   public void testCycle_01() throws Exception
   {
      test();
   }

   @Test
   public void testCycle_02() throws Exception
   {
      test();
   }

   public static enum TransformationMode
   {
      NOOP, MAVEN,
   }

   private void test() throws IOException, Exception, DependencyCollectionException
   {
      final Map<String, String> parts = TestHarness.parseTestDefinition(getClass().getSimpleName() + "/"
         + getTestName());

      final List<Model> models = TestHarness.parsePoms(parts.get("input"));
      TestHarness.deploy(repositoryFacade, models);

      final Model root = TestHarness.getRoot(models);
      if (root != null)
      {
         final File projectFile = getWs().newFile("pom.xml");
         modelWriter.write(projectFile, null, root);
         final MavenProject project = buildProject(projectFile).getProject();
         RepositorySystemSession session = buildContext.getRepositorySession();

         String actual;
         actual = TestHarness.toString(collectDependencies(session, project, TransformationMode.NOOP));
         assertEquals(parts.get("expected transformationMode=noop"), actual);

         actual = TestHarness.toString(collectDependencies(session, project, TransformationMode.MAVEN));
         assertEquals(parts.get("expected transformationMode=maven"), actual);
      }

   }

   private CollectResult collectDependencies(final RepositorySystemSession repositorySystemSession,
      final MavenProject project, final TransformationMode transformationMode) throws DependencyCollectionException
   {
      final AbstractForwardingRepositorySystemSession session = new AbstractForwardingRepositorySystemSession()
      {
         @Override
         protected RepositorySystemSession getSession()
         {
            return repositorySystemSession;
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer()
         {
            switch (transformationMode)
            {
               case NOOP :
                  return new NoopDependencyGraphTransformer();
               case MAVEN :
                  return super.getDependencyGraphTransformer();
               default :
                  throw new IllegalStateException();
            }
         }
      };

      final CollectRequest collectRequest = TestHarness.newCollectRequest(session, project);
      return dependencyCollector.collectDependencies(session, collectRequest);
   }


   @Override
   @After
   public void tearDown() throws Exception
   {
      buildContext.setSession(null);
      super.tearDown();
   }
}
