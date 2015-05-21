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

package org.sourcepit.maven.dependency.model;

import java.io.File;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.plugin.LegacySupport;
import org.eclipse.aether.graph.DependencyNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.maven.dependency.model.aether.DependencyNode2;
import org.sourcepit.maven.dependency.model.aether.DependencyNode2Adapter;

public class DependencyResolutionTest extends EmbeddedMavenEnvironmentTest {
   @Inject
   private ArtifactRepositoryFacade repositoryFacade;

   @Inject
   private LegacySupport buildContext;

   @Inject
   private DependencyTreeBuilder treeBuilder;

   @Override
   protected Environment newEnvironment() {
      return Environment.get("env-test.properties");
   }

   @Override
   @Before
   public void setUp() throws Exception {
      super.setUp();
      repositoryFacade.setEmbeddedMaven(getEmbeddedMaven());

      final MavenSession session = buildStubProject(getLocalRepositoryPath()).getSession();
      buildContext.setSession(session);
   }

   @Override
   protected File getLocalRepositoryPath() {
      return getWs().newDir("local-repo");
   }

   protected File getRemoteRepositoryPath() {
      return getWs().newDir("remote-repo");
   }

   @Override
   @After
   public void tearDown() throws Exception {
      buildContext.setSession(null);
      super.tearDown();
   }

   @Test
   public void testDepMngt() throws Exception {
      Model pom;

      pom = newModel("group", "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      addDependency(pom, "group", "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "root", "1");
      addDependency(pom, "group", "A", null);

      pom.setDependencyManagement(new DependencyManagement());
      addDependency(pom.getDependencyManagement(), "group", "A", "[1,4)");
      addDependency(pom.getDependencyManagement(), "group", "B", "[1,4)");
      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      print(DependencyNode2Adapter.get(graph), 0);
   }

   @Test
   public void testReuse() throws Exception {
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
   public void testTheE1Problem() throws Exception {
      Model pom;

      pom = newModel("group", "E", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "D", "1");
      addDependency(pom, "group", "E", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "E", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "2");
      addDependency(pom, "group", "E", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "C", "1");
      addDependency(pom, "group", "D", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      addDependency(pom, "group", "A", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
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


   public void testCycle() throws Exception {
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

      print(DependencyNode2Adapter.get(graph), 0);
   }

   @Test
   public void testLatest() throws Exception {
      Model pom;

      pom = newModel("group", "A", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "root", "1");
      addDependency(pom, "group", "A", "LATEST");
      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      print(DependencyNode2Adapter.get(graph), 0);
   }

   @Test
   public void testScopes() throws Exception {
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
   public void testScopes2() throws Exception {
      Model pom;

      pom = newModel("group", "C", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      addDependency(pom, "group", "C", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      addDependency(pom, "group", "C", "1");
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
   public void testOptional() throws Exception {
      Model pom;

      // pom = newModel("group", "B", "2");
      // repositoryFacade.deploy(pom);

      pom = newModel("group", "C", "1");
      addDependency(pom, "group", "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "A", "1");
      addDependency(pom, "group", "B", "1").setOptional(true);
      addDependency(pom, "group", "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("group", "root", "1");
      addDependency(pom, "group", "A", "1");
      repositoryFacade.deploy(pom);

      final Artifact projectArtifact = getEmbeddedMaven().createProjectArtifact(pom);

      DependencyTreeBuilderRequest request = new DependencyTreeBuilderRequest();
      request.setArtifact(projectArtifact);

      DependencyNode graph = treeBuilder.build(request);

      print(DependencyNode2Adapter.get(graph), 0);
   }

   @Test
   public void testRelocation() throws Exception {
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
   public void test() throws Exception {
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

   private void print(DependencyNode2 node, int level) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < level; i++) {
         sb.append("   ");
      }
      sb.append(node.toString());
      sb.append(" ");

      System.out.println(sb);
      level++;

      for (DependencyNode dependencyNode : node.getTarget().getChildren()) {
         print(DependencyNode2Adapter.get(dependencyNode), level);
      }
   }

   private static Model newModel(String groupId, String artifactId, String version) {
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId(groupId);
      pom.setArtifactId(artifactId);
      pom.setVersion(version);
      return pom;
   }

   private static Dependency newDependency(String groupId, String artifactId, String version) {
      final Dependency dependency = new Dependency();
      dependency.setGroupId(groupId);
      dependency.setArtifactId(artifactId);
      dependency.setVersion(version);
      return dependency;
   }

   private static void setRelocation(Model model, String groupId, String artifactId, String version) {
      DistributionManagement distributionManagement = model.getDistributionManagement();
      if (distributionManagement == null) {
         distributionManagement = new DistributionManagement();
         model.setDistributionManagement(distributionManagement);
      }

      final Relocation relocation = new Relocation();
      relocation.setGroupId(groupId);
      relocation.setArtifactId(artifactId);
      relocation.setVersion(version);

      distributionManagement.setRelocation(relocation);
   }

   private static Dependency addDependency(ModelBase model, String groupId, String artifactId, String version) {
      final Dependency dependency = newDependency(groupId, artifactId, version);
      model.getDependencies().add(dependency);
      return dependency;
   }

   private static Dependency addDependency(DependencyManagement model, String groupId, String artifactId, String version) {
      final Dependency dependency = newDependency(groupId, artifactId, version);
      model.getDependencies().add(dependency);
      return dependency;
   }
}
