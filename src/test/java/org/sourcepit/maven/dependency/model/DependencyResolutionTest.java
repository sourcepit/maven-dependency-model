/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import java.io.File;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.plugin.LegacySupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.graph.DependencyNode;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;

public class DependencyResolutionTest extends EmbeddedMavenEnvironmentTest
{
   @Inject
   private ArtifactRepositoryFacade repositoryFacade;

   @Inject
   private LegacySupport buildContext;

   @Inject
   private DependencyTreeBuilder treeBuilder;

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

   @Override
   @After
   public void tearDown() throws Exception
   {
      buildContext.setSession(null);
      super.tearDown();
   }

   @Test
   public void testReuse() throws Exception
   {
      Model pom;

      pom = newModel("group", "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      addDependency(pom, "group", "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      addDependency(pom, "group", "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "root", "1");
      addDependency(pom, "group", "A", "1");
      addDependency(pom, "group", "B", "1");
      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      print(DependencyNode2Adapter.get(graph), 0);
   }


   @Test
   public void testCycle() throws Exception
   {
      Model pom;

      pom = newModel("group", "C", "1");
      addDependency(pom, "group", "A", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      addDependency(pom, "group", "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      addDependency(pom, "group", "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "root", "1");
      addDependency(pom, "group", "A", "1");
      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      // print(DependencyNode2AdapterFactory.get(graph), 0);
   }

   @Test
   public void testScopes() throws Exception
   {
      Model pom;

      pom = newModel("group", "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      addDependency(pom, "group", "C", "1").setScope("test");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      addDependency(pom, "group", "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "root", "1");
      addDependency(pom, "group", "A", "1").setScope("test");
      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      print(DependencyNode2Adapter.get(graph), 0);
   }

   @Test
   public void testScopes2() throws Exception
   {
      Model pom;

      pom = newModel("group", "A", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "C", "1");
      // addDependency(pom, "group", "A", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      addDependency(pom, "group", "A", "2").setScope("test");
      ;
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "root", "1");
      addDependency(pom, "group", "A", "1");
      addDependency(pom, "group", "B", "1");
      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      print(DependencyNode2Adapter.get(graph), 0);
   }

   @Test
   public void testRelocation() throws Exception
   {
      Model pom;

      pom = newModel("group", "C", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "2");
      addDependency(pom, "group", "C", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      addDependency(pom, "group", "A", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      setRelocation(pom, "group", "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "root", "1");
      addDependency(pom, "group", "A", "1");
      addDependency(pom, "group", "B", "1");
      addDependency(pom, "group", "C", "1");
      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      print(DependencyNode2Adapter.get(graph), 0);
   }

   @Test
   public void test() throws Exception
   {
      Model pom;
      pom = newModel("group", "D", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "C", "1");
      // addDependency(pom, "group", "D", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      addDependency(pom, "group", "C", "1");
      addDependency(pom, "group", "D", "1").setOptional(true);
      repositoryFacade.deploy(pom);

      pom = newModel("group", "D", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "C", "2");
      // addDependency(pom, "group", "D", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      addDependency(pom, "group", "B", "[1,2)");
      addDependency(pom, "group", "C", "2");
      addDependency(pom, "group", "D", "2").setScope("test");

      File newFile = getWs().newFile("pom.xml");
      new DefaultModelWriter().write(newFile, null, pom);

      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      print(DependencyNode2Adapter.get(graph), 0);
   }

   private void print(DependencyNode2 node, int level)
   {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < level; i++)
      {
         sb.append("   ");
      }
      sb.append(node.toString());
      sb.append(" ");

      System.out.println(sb);
      level++;

      for (DependencyNode dependencyNode : node.getTarget().getChildren())
      {
         print(DependencyNode2Adapter.get(dependencyNode), level);
      }
   }

   private static Model newModel(String groupId, String artifactId, String version)
   {
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId(groupId);
      pom.setArtifactId(artifactId);
      pom.setVersion(version);
      return pom;
   }

   private static Dependency newDependency(String groupId, String artifactId, String version)
   {
      final Dependency dependency = new Dependency();
      dependency.setGroupId(groupId);
      dependency.setArtifactId(artifactId);
      dependency.setVersion(version);
      return dependency;
   }

   private static void setRelocation(Model model, String groupId, String artifactId, String version)
   {
      DistributionManagement distributionManagement = model.getDistributionManagement();
      if (distributionManagement == null)
      {
         distributionManagement = new DistributionManagement();
         model.setDistributionManagement(distributionManagement);
      }

      final Relocation relocation = new Relocation();
      relocation.setGroupId(groupId);
      relocation.setArtifactId(artifactId);
      relocation.setVersion(version);

      distributionManagement.setRelocation(relocation);
   }

   private static Dependency addDependency(Model model, String groupId, String artifactId, String version)
   {
      final Dependency dependency = newDependency(groupId, artifactId, version);
      model.getDependencies().add(dependency);
      return dependency;
   }
}
