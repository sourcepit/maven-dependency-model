/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.UnsupportedEncodingException;
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

public class DefaultDependencyGraphSkeletonBuilderTest extends EmbeddedMavenEnvironmentTest
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

   private DependencyGraphSkeletonBuilder skeletonBuilder;

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      repositoryFacade.setEmbeddedMaven(getEmbeddedMaven());

      final MavenSession session = buildStubProject(getLocalRepositoryPath()).getSession();
      buildContext.setSession(session);

      skeletonBuilder = new DefaultDependencyGraphSkeletonBuilder(dependencyCollector);
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
   public void testName() throws Exception
   {
      final Map<String, String> testDefinition = TestHarness
         .parseTestDefinition("DependencyCollectionTest/VersionConflict_01");

      final List<Model> models = TestHarness.parsePoms(testDefinition.get("input"));
      TestHarness.deploy(repositoryFacade, models);

      final Model root = TestHarness.getRoot(models);
      if (root != null)
      {
         final File projectFile = getWs().newFile("pom.xml");
         modelWriter.write(projectFile, null, root);
         final RepositorySystemSession session = buildContext.getRepositorySession();
         final MavenProject project = buildProject(projectFile).getProject();

         testBuildGraphSkeleton(session, project);

         testBuildGraphsSkeleton(session, project, testDefinition);
      }
   }

   private void testBuildGraphsSkeleton(RepositorySystemSession session, MavenProject project,
      Map<String, String> testDefinition) throws UnsupportedEncodingException, DependencyCollectionException
   {
      assertEquals(testDefinition.get("expected buildGraphsSkeleton conflictResolutionScope=OVERALL_TREE"),
         TestHarness.toString(buildGraphsSkeleton(session, project, ConflictResolutionScope.OVERALL_TREE)));

      assertEquals(testDefinition.get("expected buildGraphsSkeleton conflictResolutionScope=ISOLATED_TREES"),
         TestHarness.toString(buildGraphsSkeleton(session, project, ConflictResolutionScope.ISOLATED_TREES)));
   }

   private CollectResult buildGraphsSkeleton(RepositorySystemSession session, MavenProject project,
      final ConflictResolutionScope conflictScope) throws DependencyCollectionException
   {
      final GraphsRequest request = GraphRequestUtil.newGraphsRequest(session, project,
         DependencyResolutionScope.COMPILE_AND_RUNTIME, conflictScope);
      return skeletonBuilder.buildGraphsSkeleton(session, request);
   }

   private void testBuildGraphSkeleton(RepositorySystemSession session, MavenProject project)
      throws UnsupportedEncodingException, DependencyCollectionException
   {
      // test maven compatibility
      final String expected = TestHarness.toString(mvnCollect(dependencyCollector, project, session));
      final String actual = TestHarness.toString(buildSkeleton(skeletonBuilder, project, session));
      assertEquals(expected, actual);
   }

   private CollectResult buildSkeleton(final DependencyGraphSkeletonBuilder skeletonBuilder,
      final MavenProject project, final RepositorySystemSession session) throws DependencyCollectionException
   {
      GraphRequest request = GraphRequestUtil.newGraphRequest(session, project,
         DependencyResolutionScope.COMPILE_AND_RUNTIME);
      return skeletonBuilder.buildGraphSkeleton(session, request);
   }

   private static CollectResult mvnCollect(DependencyCollector collector, final MavenProject project,
      final RepositorySystemSession session) throws DependencyCollectionException
   {
      final CollectRequest collectRequest = TestHarness.newCollectRequest(session, project);
      return collector.collectDependencies(new AbstractForwardingRepositorySystemSession()
      {
         @Override
         protected RepositorySystemSession getSession()
         {
            return session;
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer()
         {
            return new NoopDependencyGraphTransformer();
         }
      }, collectRequest);
   }

}
