/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.resolution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.emf.common.util.EList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.util.FilterRepositorySystemSession;
import org.sonatype.aether.util.filter.ScopeDependencyFilter;
import org.sonatype.aether.util.graph.DefaultDependencyNode;
import org.sonatype.aether.util.graph.selector.StaticDependencySelector;
import org.sonatype.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.common.maven.model.util.MavenModelUtils;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.common.utils.lang.Exceptions;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyNode;
import org.sourcepit.maven.dependency.model.DependencyTree;

public class DependencyResolutionTest extends EmbeddedMavenEnvironmentTest
{
   @Inject
   private ArtifactRepositoryFacade repositoryFacade;

   @Inject
   private LegacySupport buildContext;

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

      DependencyModel model = resolve(rootArtifact);
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

      DependencyModel model = resolve(rootArtifact);
      assertEquals(2, model.getArtifacts().size());
      assertEquals(2, model.getDependencyTrees().size());

      MavenArtifact b = model.getArtifacts().get(0);
      assertEquals("B", b.getArtifactId());
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

      DependencyModel model = resolve(rootArtifact);
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

      pom = newModel("B", "1");
      addDependency(pom, "A", "1");
      repositoryFacade.deploy(pom);

      pom = newModel("A", "1");
      addDependency(pom, "B", "1");
      repositoryFacade.deploy(pom);

      final Artifact rootArtifact = getEmbeddedMaven().createArtifact(pom);

      try
      {
         resolve(rootArtifact);
         fail();
      }
      catch (IllegalStateException e)
      {
      }
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

      DependencyModel model = resolve(dependencies);
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

      DependencyModel model = resolve(artifact);

      EList<MavenArtifact> artifacts = model.getArtifacts();
      assertEquals(3, artifacts.size());

      MavenArtifact artifactC = artifacts.get(0);
      assertEquals("C", artifactC.getArtifactId());
      MavenArtifact artifactB = artifacts.get(1);
      assertEquals("B", artifactB.getArtifactId());
      MavenArtifact artifactA = artifacts.get(2);
      assertEquals("A", artifactA.getArtifactId());

      assertEquals(3, model.getDependencyTrees().size());

      DependencyTree tree;

      tree = model.getDependencyTrees().get(0);
      assertSame(artifactC, tree.getTargetArtifact());
      assertEquals(0, tree.getDependencyNodes().size());

      tree = model.getDependencyTrees().get(1);
      assertSame(artifactB, tree.getTargetArtifact());
      assertEquals(1, tree.getDependencyNodes().size());

      DependencyNode node;
      node = tree.getDependencyNodes().get(0);
      assertSame(artifactC, node.getArtifact());
      assertSame(Scope.COMPILE, node.getEffectiveScope());
      assertTrue(node.isSelected());

      tree = model.getDependencyTrees().get(2);
      assertSame(artifactA, tree.getTargetArtifact());
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

      DependencyModel model = resolve(artifact);

      EList<MavenArtifact> artifacts = model.getArtifacts();
      assertEquals(2, artifacts.size());
      assertEquals(2, model.getDependencyTrees().size());

      MavenArtifact artifactB = artifacts.get(0);
      assertEquals("B", artifactB.getArtifactId());
      MavenArtifact artifactA = artifacts.get(1);
      assertEquals("A", artifactA.getArtifactId());

      DependencyTree tree;
      tree = model.getDependencyTrees().get(0);
      assertSame(artifactB, tree.getTargetArtifact());
      assertEquals(1, tree.getDependencyNodes().size());
      
      DependencyNode node;
      node = tree.getDependencyNodes().get(0);
      assertSame(null, node.getArtifact());
      assertSame(Scope.TEST, node.getEffectiveScope());
      assertFalse(node.isSelected());

      tree = model.getDependencyTrees().get(1);
      assertSame(artifactA, tree.getTargetArtifact());
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

   public DependencyModel resolve(@NotNull Collection<Dependency> dependencies) throws ProjectBuildingException,
      DependencyResolutionException
   {
      final Model model;

      final MavenProject currentProject = buildContext.getSession().getCurrentProject();
      if (currentProject == null)
      {
         model = new Model();
      }
      else
      {
         model = currentProject.getModel().clone();
      }

      model.setModelVersion("4.0.0");
      model.setGroupId("org.sourcepit");
      model.setArtifactId("dummy-project");
      model.setVersion("1337");

      model.getDependencies().clear();
      model.getDependencies().addAll(dependencies);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      try
      {
         new DefaultModelWriter().write(out, null, model);
      }
      catch (IOException e)
      {
         throw Exceptions.pipe(e);
      }
      final byte[] bytes = out.toByteArray();

      final ProjectBuildingRequest request = newProjectBuildingRequest(false, false);

      ProjectBuildingResult result = projectBuilder.build(new ModelSource()
      {
         @Override
         public String getLocation()
         {
            return "memory";
         }

         @Override
         public InputStream getInputStream() throws IOException
         {
            return new ByteArrayInputStream(bytes);
         }
      }, request);

      final MavenProject project = result.getProject();
      return resolve(project, false);
   }

   public DependencyModel resolve(@NotNull Artifact artifact) throws ProjectBuildingException,
      DependencyResolutionException
   {
      final MavenProject project = buildProject(artifact);
      return resolve(project);
   }

   public DependencyModel resolve(@NotNull MavenProject project) throws DependencyResolutionException
   {
      return resolve(project, true);
   }

   @Inject
   private ProjectDependenciesResolver resolver;

   private DependencyModel resolve(@NotNull MavenProject project, boolean resolveRoot)
      throws DependencyResolutionException
   {
      final DependencyModelBuilder modelBuilder = new DependencyModelBuilder();

      final DependencyGraphTransformer transformer;
      if (resolveRoot)
      {
         DefaultDependencyNode rootNode = new DefaultDependencyNode();
         rootNode.setDependency(new org.sonatype.aether.graph.Dependency(RepositoryUtils.toArtifact(project
            .getArtifact()), "compile"));
         rootNode.setRepositories(project.getRemoteProjectRepositories());
         rootNode.setRequestContext("project");

         transformer = new ChainedDependencyGraphTransformer(new ReplaceRootNode(rootNode),
            new DependencyModelBuildingGraphTransformer(modelBuilder, true, false));
      }
      else
      {
         transformer = new DependencyModelBuildingGraphTransformer(modelBuilder, true, false);
      }

      final RepositorySystemSession repositorySession = new FilterRepositorySystemSession(
         buildContext.getRepositorySession())
      {
         @Override
         public DependencySelector getDependencySelector()
         {
            return new StaticDependencySelector(true);
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer()
         {
            return transformer;
         }
      };

      final DefaultDependencyResolutionRequest resolutionRequest = new DefaultDependencyResolutionRequest();
      resolutionRequest.setMavenProject(project);
      resolutionRequest.setRepositorySession(repositorySession);
      resolutionRequest.setResolutionFilter(new ScopeDependencyFilter("test"));

      DependencyResolutionResult resolutionResult = resolver.resolve(resolutionRequest);

      DependencyModel model = modelBuilder.getDependencyModel();

      applyResolvedArtifacts(project, resolutionResult, model);

      return model;
   }

   private void applyResolvedArtifacts(MavenProject project, DependencyResolutionResult resolutionResult,
      DependencyModel model)
   {
      Map<ArtifactKey, MavenArtifact> foo = new HashMap<ArtifactKey, MavenArtifact>();

      EList<MavenArtifact> artifacts2 = model.getArtifacts();
      for (MavenArtifact mavenArtifact : artifacts2)
      {
         foo.put(mavenArtifact.getArtifactKey(), mavenArtifact);
      }

      List<org.sonatype.aether.graph.Dependency> resolvedDependencies = resolutionResult.getResolvedDependencies();
      for (org.sonatype.aether.graph.Dependency dependency : resolvedDependencies)
      {
         org.sonatype.aether.artifact.Artifact artifact = dependency.getArtifact();
         final ArtifactKey artifactKey = MavenModelUtils.toArtifactKey(artifact);
         foo.get(artifactKey).setFile(artifact.getFile());
      }
   }

   @Inject
   private ProjectBuilder projectBuilder;

   private MavenProject buildProject(final Artifact artifact) throws ProjectBuildingException
   {
      final ProjectBuildingRequest request = newProjectBuildingRequest(false, false);

      ProjectBuildingResult build = projectBuilder.build(artifact, request);
      return build.getProject();
   }

   private ProjectBuildingRequest newProjectBuildingRequest(boolean resolveDeps, boolean processPlugins)
   {
      final ProjectBuildingRequest request = new DefaultProjectBuildingRequest(buildContext.getSession()
         .getProjectBuildingRequest());
      request.setResolveDependencies(resolveDeps);
      request.setProcessPlugins(processPlugins);
      request.setProject(null);

      final MavenProject project = buildContext.getSession().getCurrentProject();
      if (project != null)
      {
         @SuppressWarnings("unchecked")
         List<ArtifactRepository> artifactRepos = combine(project.getRemoteArtifactRepositories(),
            request.getRemoteRepositories());

         @SuppressWarnings("unchecked")
         List<ArtifactRepository> pluginRepos = combine(project.getPluginArtifactRepositories(),
            request.getPluginArtifactRepositories());

         request.setRemoteRepositories(artifactRepos);
         request.setPluginArtifactRepositories(pluginRepos);
      }

      return request;
   }

   private static List<ArtifactRepository> combine(List<ArtifactRepository>... repoLists)
   {
      final Set<String> ids = new HashSet<String>();
      final List<ArtifactRepository> result = new ArrayList<ArtifactRepository>();

      for (List<ArtifactRepository> repos : repoLists)
      {
         for (ArtifactRepository repo : repos)
         {
            if (ids.add(repo.getId()))
            {
               result.add(repo);
            }
         }
      }

      return result;
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
