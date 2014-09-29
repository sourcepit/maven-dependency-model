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

package org.sourcepit.maven.dependency.model.aether;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.eclipse.emf.common.util.EList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.maven.testing.MavenExecutionResult2;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyModelResolver;
import org.sourcepit.maven.dependency.model.DependencyNode;
import org.sourcepit.maven.dependency.model.DependencyTree;
import org.sourcepit.maven.dependency.model.JavaSourceAttachmentFactory;

public class AetherDependencyModelResolverTest extends EmbeddedMavenEnvironmentTest
{
   @Inject
   private ArtifactRepositoryFacade repositoryFacade;

   @Inject
   private LegacySupport buildContext;

   @Inject
   @Named("aether")
   private DependencyModelResolver modelResolver;

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
   public void testRootIsResolved() throws Exception
   {
      Model pom;

      pom = newModel("A", "1");
      repositoryFacade.deploy(pom);

      final Artifact rootArtifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(rootArtifact, null);
      assertEquals(1, model.getArtifacts().size());
      assertEquals(1, model.getDependencyTrees().size());

      MavenArtifact b = model.getArtifacts().get(0);
      assertEquals("A", b.getArtifactId());
      assertNotNull(b.getFile());
      assertEquals(true, b.getFile().exists());
   }

   @Test
   public void testSnapshot() throws Exception
   {
      Model pom;

      pom = newModel("B", "1.0.0-SNAPSHOT");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1.0.0-SNAPSHOT");
      repositoryFacade.deploy(pom);

      final Artifact rootArtifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(rootArtifact, null);
      assertEquals(2, model.getArtifacts().size());
      assertEquals(2, model.getDependencyTrees().size());

      MavenArtifact b = model.getArtifacts().get(1);
      assertEquals("B", b.getArtifactId());
      assertEquals("1.0.0-SNAPSHOT", b.getVersion());
      assertNotNull(b.getFile());
      assertEquals(true, b.getFile().exists());
   }

   @Test
   public void testDuplicatedNodes() throws Exception
   {
      Model pom;

      pom = newModel("D", "1");
      addDependency(pom, "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("B", "1");
      addDependency(pom, "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1");
      addDependency(pom, "D", "1");
      repositoryFacade.deploy(pom);

      final Artifact rootArtifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(rootArtifact, null);
      assertEquals(4, model.getArtifacts().size());
      assertEquals(4, model.getDependencyTrees().size());

      for (MavenArtifact artifact : model.getArtifacts())
      {
         assertTrue(artifact.getFile().exists());
      }
   }

   @Test
   public void testCycle() throws Exception
   {
      Model pom;

      pom = newModel("C", "1");
      addDependency(pom, "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("B", "1");
      addDependency(pom, "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1");
      repositoryFacade.deploy(pom);

      final Artifact rootArtifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(rootArtifact, null);
      assertEquals(3, model.getDependencyTrees().size());

      EList<MavenArtifact> artifacts = model.getArtifacts();
      assertEquals(3, artifacts.size());

      MavenArtifact artifactA = artifacts.get(0);
      assertEquals("A", artifactA.getArtifactId());
      MavenArtifact artifactB = artifacts.get(1);
      assertEquals("B", artifactB.getArtifactId());
      MavenArtifact artifactC = artifacts.get(2);
      assertEquals("C", artifactC.getArtifactId());

      DependencyTree tree;
      tree = model.getDependencyTree(artifactA);
      assertSame(artifactA, tree.getArtifact());
      assertEquals(1, tree.getDependencyNodes().size());

      DependencyNode node;
      node = tree.getDependencyNodes().get(0);
      assertSame(artifactB, node.getArtifact());
      assertTrue(node.isSelected());
      assertEquals(1, node.getChildren().size());

      node = node.getChildren().get(0);
      assertSame(artifactC, node.getArtifact());
      assertTrue(node.isSelected());
      assertEquals(1, node.getChildren().size());

      node = node.getChildren().get(0);
      assertSame(artifactB, node.getArtifact());
      assertFalse(node.isSelected());
      assertEquals(0, node.getChildren().size());
      assertSame(node.eContainer().eContainer(), node.getConflictNode());
      assertSame(node.eContainer().eContainer(), node.getCycleNode());

      tree = model.getDependencyTree(artifactB);
      assertSame(artifactB, tree.getArtifact());
      assertEquals(1, tree.getDependencyNodes().size());

      node = tree.getDependencyNodes().get(0);
      assertSame(artifactC, node.getArtifact());
      assertTrue(node.isSelected());
      assertEquals(1, node.getChildren().size());

      node = node.getChildren().get(0);
      assertSame(artifactB, node.getArtifact());
      assertFalse(node.isSelected());
      assertEquals(0, node.getChildren().size());
      assertNull(node.getCycleNode()); // cycle with tree

      tree = model.getDependencyTree(artifactC);
      assertSame(artifactC, tree.getArtifact());
      assertEquals(1, tree.getDependencyNodes().size());

      node = tree.getDependencyNodes().get(0);
      assertSame(artifactB, node.getArtifact());
      assertTrue(node.isSelected());
      assertEquals(1, node.getChildren().size());

      node = node.getChildren().get(0);
      assertSame(artifactC, node.getArtifact());
      assertFalse(node.isSelected());
      assertEquals(0, node.getChildren().size());
      assertNull(node.getCycleNode()); // cycle with tree
   }

   @Test
   public void testDependencies() throws Exception
   {
      Model pom;

      pom = newModel("B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      repositoryFacade.deploy(pom);

      List<Dependency> dependencies = new ArrayList<Dependency>();
      dependencies.add(newDependency("A", "1"));
      dependencies.add(newDependency("B", "1"));

      DependencyModel model = modelResolver.resolve(dependencies, null);
      assertEquals(2, model.getArtifacts().size());
      assertEquals(2, model.getDependencyTrees().size());
   }

   @Test
   public void testScopeTest() throws Exception
   {
      Model pom;

      pom = newModel("C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("B", "1");
      addDependency(pom, "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1");
      addDependency(pom, "C", "1").setScope("test");
      repositoryFacade.deploy(pom);

      final Artifact artifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(artifact, null);

      EList<MavenArtifact> artifacts = model.getArtifacts();
      assertEquals(3, artifacts.size());

      MavenArtifact artifactA = artifacts.get(0);
      assertEquals("A", artifactA.getArtifactId());
      MavenArtifact artifactB = artifacts.get(1);
      assertEquals("B", artifactB.getArtifactId());
      MavenArtifact artifactC = artifacts.get(2);
      assertEquals("C", artifactC.getArtifactId());

      assertEquals(3, model.getDependencyTrees().size());

      DependencyTree tree;

      tree = model.getDependencyTree(artifactC);
      assertSame(artifactC, tree.getArtifact());
      assertEquals(0, tree.getDependencyNodes().size());

      tree = model.getDependencyTree(artifactB);
      assertSame(artifactB, tree.getArtifact());
      assertEquals(1, tree.getDependencyNodes().size());

      DependencyNode node;
      node = tree.getDependencyNodes().get(0);
      assertSame(artifactC, node.getArtifact());
      assertSame(Scope.COMPILE, node.getEffectiveScope());
      assertTrue(node.isSelected());

      tree = model.getDependencyTree(artifactA);
      assertSame(artifactA, tree.getArtifact());
      assertEquals(2, tree.getDependencyNodes().size());

      node = tree.getDependencyNodes().get(0);
      assertSame(artifactB, node.getArtifact());
      assertSame(Scope.COMPILE, node.getEffectiveScope());
      assertTrue(node.isSelected());

      node = tree.getDependencyNodes().get(1);
      assertSame(artifactC, node.getArtifact());
      assertSame(Scope.TEST, node.getEffectiveScope());
      assertFalse(node.isSelected());
   }

   @Test
   public void testScopeTest2() throws Exception
   {
      Model pom;

      pom = newModel("C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("B", "1");
      addDependency(pom, "C", "1").setScope("test");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1");
      addDependency(pom, "C", "1").setScope("test");
      repositoryFacade.deploy(pom);

      final Artifact artifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(artifact, null);

      EList<MavenArtifact> artifacts = model.getArtifacts();
      assertEquals(2, artifacts.size());
      assertEquals(2, model.getDependencyTrees().size());

      MavenArtifact artifactA = artifacts.get(0);
      assertEquals("A", artifactA.getArtifactId());
      MavenArtifact artifactB = artifacts.get(1);
      assertEquals("B", artifactB.getArtifactId());

      DependencyTree tree;
      tree = model.getDependencyTree(artifactB);
      assertSame(artifactB, tree.getArtifact());
      assertEquals(1, tree.getDependencyNodes().size());

      DependencyNode node;
      node = tree.getDependencyNodes().get(0);
      assertSame(null, node.getArtifact());
      assertSame(Scope.TEST, node.getEffectiveScope());
      assertFalse(node.isSelected());

      tree = model.getDependencyTree(artifactA);
      assertSame(artifactA, tree.getArtifact());
      assertEquals(2, tree.getDependencyNodes().size());

      node = tree.getDependencyNodes().get(0);
      assertSame(artifactB, node.getArtifact());
      assertSame(Scope.COMPILE, node.getEffectiveScope());
      assertTrue(node.isSelected());

      node = tree.getDependencyNodes().get(1);
      assertSame(null, node.getArtifact());
      assertSame(Scope.TEST, node.getEffectiveScope());
      assertFalse(node.isSelected());
   }

   @Test
   public void testScopeTest_EraseChildDepsOfTestDeps() throws Exception
   {
      Model pom;
      pom = newModel("C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("B", "1");
      addDependency(pom, "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1").setScope("test");
      repositoryFacade.deploy(pom);

      final Artifact artifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(artifact, null);

      EList<MavenArtifact> artifacts = model.getArtifacts();
      assertEquals(1, artifacts.size());
      assertEquals(1, model.getDependencyTrees().size());

      MavenArtifact artifactA = artifacts.get(0);
      assertEquals("A", artifactA.getArtifactId());

      DependencyTree tree;
      tree = model.getDependencyTrees().get(0);
      assertSame(artifactA, tree.getArtifact());
      assertEquals(1, tree.getDependencyNodes().size());

      DependencyNode node;
      node = tree.getDependencyNodes().get(0);
      assertSame(null, node.getArtifact());
      assertSame(Scope.TEST, node.getEffectiveScope());
      assertFalse(node.isSelected());
      assertEquals(0, node.getChildren().size());
   }

   @Test
   public void testScope_FallbackToNonTestNode() throws Exception
   {
      Model pom;

      pom = newModel("C", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("B", "2");
      addDependency(pom, "C", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("B", "1");
      addDependency(pom, "C", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("root", "1");
      addDependency(pom, "A", "1");
      addDependency(pom, "B", "2").setScope("test");
      repositoryFacade.deploy(pom);

      final Artifact artifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(artifact, null);

      for (DependencyTree tree : model.getDependencyTrees())
      {
         for (DependencyNode node : tree.getDependencyNodes())
         {
            assertArtifactsNotNull(node);
         }
      }
   }

   private void assertArtifactsNotNull(DependencyNode node)
   {
      assertNotNull(node.getArtifact());
      for (DependencyNode childNode : node.getChildren())
      {
         assertArtifactsNotNull(childNode);
      }
   }

   @Test
   public void testLatest() throws Exception
   {
      Model pom;

      pom = newModel("B", "1");
      pom.setPackaging("maven-plugin"); // Aether doesn't set latest for other packaging types on deploy
      repositoryFacade.deploy(pom);

      pom = newModel("B", "2-SNAPSHOT");
      pom.setPackaging("maven-plugin"); // Aether doesn't set latest for other packaging types on deploy
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "LATEST");
      repositoryFacade.deploy(pom);

      final Artifact artifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(artifact, null);
      assertEquals(2, model.getArtifacts().size());
      assertEquals(2, model.getDependencyTrees().size());

      MavenArtifact b = model.getArtifacts().get(1);
      assertEquals("B", b.getArtifactId());
      assertEquals("2-SNAPSHOT", b.getVersion());
      assertNotNull(b.getFile());
      assertEquals(true, b.getFile().exists());

      final MavenArtifact artifactA = model.getArtifacts().get(0);
      DependencyNode node = model.getDependencyTree(artifactA).getDependencyNodes().get(0);
      assertEquals("LATEST", node.getEffectiveVersionConstraint());
   }

   @Test
   public void testRelease() throws Exception
   {
      Model pom;

      pom = newModel("B", "2-SNAPSHOT");
      pom.setPackaging("maven-plugin"); // Aether doesn't set latest for other packaging types on deploy
      repositoryFacade.deploy(pom);

      pom = newModel("B", "2");
      pom.setPackaging("maven-plugin");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "RELEASE");
      repositoryFacade.deploy(pom);

      final Artifact artifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(artifact, null);
      assertEquals(2, model.getArtifacts().size());
      assertEquals(2, model.getDependencyTrees().size());

      MavenArtifact b = model.getArtifacts().get(1);
      assertEquals("B", b.getArtifactId());
      assertEquals("2", b.getVersion());
      assertNotNull(b.getFile());
      assertEquals(true, b.getFile().exists());

      final MavenArtifact artifactA = model.getArtifacts().get(0);
      DependencyNode node = model.getDependencyTree(artifactA).getDependencyNodes().get(0);
      assertEquals("RELEASE", node.getEffectiveVersionConstraint());
   }

   @Test
   public void testVersionRange() throws Exception
   {
      Model pom;

      pom = newModel("B", "2");
      repositoryFacade.deploy(pom);

      pom = newModel("B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "[1,2)");
      repositoryFacade.deploy(pom);

      final Artifact artifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(artifact, null);
      assertEquals(2, model.getArtifacts().size());
      assertEquals(2, model.getDependencyTrees().size());

      MavenArtifact b = model.getArtifacts().get(1);
      assertEquals("B", b.getArtifactId());
      assertEquals("1", b.getVersion());
      assertNotNull(b.getFile());
      assertEquals(true, b.getFile().exists());

      final MavenArtifact artifactA = model.getArtifacts().get(0);
      DependencyNode node = model.getDependencyTree(artifactA).getDependencyNodes().get(0);
      assertEquals("[1,2)", node.getEffectiveVersionConstraint());
   }

   @Test
   public void testSourceAttachment() throws Exception
   {
      Model pom;

      pom = newModel("B", "1");
      pom.setPackaging("java-source"); // abused as type
      repositoryFacade.deploy(pom);

      pom = newModel("B", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1");
      repositoryFacade.deploy(pom);

      final Artifact artifact = getEmbeddedMaven().createArtifact(pom);

      DependencyModel model = modelResolver.resolve(artifact, new JavaSourceAttachmentFactory());
      assertEquals(4, model.getArtifacts().size());

      MavenArtifact artifactA = model.getArtifacts().get(0);
      assertEquals("A", artifactA.getArtifactId());
      assertNull(artifactA.getClassifier());
      assertNotNull(artifactA.getFile());

      MavenArtifact sourceA = model.getArtifacts().get(1);
      assertEquals("A", sourceA.getArtifactId());
      assertEquals("sources", sourceA.getClassifier());
      assertEquals("java-source", sourceA.getType());
      assertNull(sourceA.getFile());

      MavenArtifact artifactB = model.getArtifacts().get(2);
      assertEquals("B", artifactB.getArtifactId());
      assertNull(artifactB.getClassifier());
      assertNotNull(artifactB.getFile());

      MavenArtifact sourceB = model.getArtifacts().get(3);
      assertEquals("B", sourceB.getArtifactId());
      assertEquals("sources", sourceB.getClassifier());
      assertEquals("java-source", sourceB.getType());
      assertNotNull(sourceB.getFile());
   }

   @Test
   public void testModuleProject() throws Exception
   {
      Model pom;

      pom = newModel("parent", "1");
      pom.setPackaging("pom");
      pom.getModules().add("module-a");
      pom.getModules().add("module-b");

      final File parentPom = new File(getWs().getRoot(), "pom.xml");
      new DefaultModelWriter().write(parentPom, null, pom);

      final File aPom = new File(getWs().getRoot(), "module-a/pom.xml");
      pom = newModel("module-a", "1");
      new DefaultModelWriter().write(aPom, null, pom);

      final File bPom = new File(getWs().getRoot(), "module-b/pom.xml");
      pom = newModel("module-b", "1");
      addDependency(pom, "module-a", "1");
      new DefaultModelWriter().write(bPom, null, pom);

      MavenExecutionResult2 result = buildProject(parentPom);

      MavenSession session = result.getSession();
      for (MavenProject project : session.getProjects()) // fake compile for ReactorReader
      {
         project.addLifecyclePhase("compile");
      }
      MavenProject projectB = session.getProjects().get(1);
      assertEquals("module-b", projectB.getArtifactId());
      session.setCurrentProject(projectB);

      buildContext.setSession(session);

      DependencyModel model = modelResolver.resolve(projectB.getArtifact(), null);
      assertEquals(2, model.getArtifacts().size());
      assertEquals(2, model.getDependencyTrees().size());

      MavenArtifact artifactB = model.getArtifacts().get(0);
      assertEquals("module-b", artifactB.getArtifactId());
      assertNotNull(artifactB.getFile());

      MavenArtifact artifactA = model.getArtifacts().get(1);
      assertEquals("module-a", artifactA.getArtifactId());
      assertNotNull(artifactA.getFile());

      DependencyTree tree = model.getDependencyTree(artifactB);
      assertEquals(1, tree.getDependencyNodes().size());

      DependencyNode nodeA = tree.getDependencyNodes().get(0);
      assertSame(artifactA, nodeA.getArtifact());
   }

   private static Model newModel(String artifactId, String version)
   {
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId(artifactId.toLowerCase());
      pom.setArtifactId(artifactId);
      pom.setVersion(version);
      return pom;
   }

   private static Dependency newDependency(String artifactId, String version)
   {
      final Dependency dependency = new Dependency();
      dependency.setGroupId(artifactId.toLowerCase());
      dependency.setArtifactId(artifactId);
      dependency.setVersion(version);
      return dependency;
   }

   private static Dependency addDependency(ModelBase model, String artifactId, String version)
   {
      final Dependency dependency = newDependency(artifactId, version);
      model.getDependencies().add(dependency);
      return dependency;
   }
}
