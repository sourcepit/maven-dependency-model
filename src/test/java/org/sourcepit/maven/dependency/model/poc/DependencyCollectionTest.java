/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import static org.junit.Assert.assertEquals;
import static org.sourcepit.common.utils.io.IO.cpIn;
import static org.sourcepit.common.utils.io.IO.read;
import static org.sourcepit.maven.dependency.model.poc.ConflictResolutionScope.OVERALL_TREE;
import static org.sourcepit.maven.dependency.model.poc.DependencyResolutionScope.COMPILE_AND_RUNTIME;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.inject.Inject;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.ModelWriter;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.util.artifact.ArtifactIdUtils;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.graph.transformer.NoopDependencyGraphTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.common.utils.io.IOHandle;
import org.sourcepit.common.utils.io.Read;
import org.sourcepit.maven.dependency.model.aether.DependencyGraphParser;

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
      final Map<String, String> parts = TestDefinitionHarness.parseTestDefinition(getClass().getSimpleName() + "/"
         + getTestName());

      final List<Model> models = parsePoms(parts.get("input"));
      deploy(models);

      final Model root = getRoot(models);
      if (root != null)
      {
         final File projectFile = getWs().newFile("pom.xml");
         modelWriter.write(projectFile, null, root);
         final MavenProject project = buildProject(projectFile).getProject();
         RepositorySystemSession session = buildContext.getRepositorySession();

         String actual;
         actual = toString(collectDependencies(session, project, TransformationMode.NOOP));
         assertEquals(parts.get("expected transformationMode=noop"), actual);

         actual = toString(collectDependencies(session, project, TransformationMode.MAVEN));
         assertEquals(parts.get("expected transformationMode=maven"), actual);
      }

   }

   private static String toString(CollectResult collectResult) throws UnsupportedEncodingException
   {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final Stack<DependencyNode> parentNodes = new Stack<DependencyNode>();
      print(new PrintStream(out, false, "UTF-8"), parentNodes, collectResult.getRoot(), 0);
      return new String(out.toByteArray(), "UTF-8");
   }

   private static Model getRoot(List<Model> models)
   {
      for (Model model : models)
      {
         if ("true".equals(model.getProperties().getProperty("root")))
         {
            return model;
         }
      }
      return null;
   }

   private void deploy(final List<Model> models)
   {
      for (Model model : models)
      {
         if ("true".equals(model.getProperties().getProperty("deploy", "true")))
         {
            repositoryFacade.deploy(model);
         }
      }
   }

   private static List<Model> parsePoms(String input) throws IOException
   {
      List<DependencyNode> graphs = new DependencyGraphParser().parseMultipleLiteral(input);
      List<Model> poms = new ArrayList<Model>(graphs.size());
      for (DependencyNode graph : graphs)
      {
         final Model pom = toPom(graph.getDependency().getArtifact());
         for (DependencyNode child : graph.getChildren())
         {
            pom.addDependency(toDependency(child));
         }

         poms.add(pom);
      }
      return poms;
   }

   private static Dependency toDependency(DependencyNode node)
   {
      org.eclipse.aether.graph.Dependency dependency = node.getDependency();

      org.eclipse.aether.artifact.Artifact artifact = dependency.getArtifact();

      Dependency dep = new Dependency();
      dep.setGroupId(artifact.getGroupId());
      dep.setArtifactId(artifact.getArtifactId());
      dep.setType(artifact.getExtension());
      dep.setClassifier(artifact.getClassifier());

      dep.setScope(dependency.getScope());
      dep.setOptional(dependency.isOptional());

      dep.setSystemPath(artifact.getProperty(ArtifactProperties.LOCAL_PATH, null));

      dep.setVersion(node.getVersionConstraint().toString());

      return dep;
   }

   private static Model toPom(org.eclipse.aether.artifact.Artifact artifact)
   {
      final Model pom = new Model();
      pom.setModelVersion("4.0.0");
      pom.setGroupId(artifact.getGroupId());
      pom.setArtifactId(artifact.getArtifactId());
      pom.setVersion(artifact.getVersion());

      final String extension = artifact.getExtension();
      if (extension != null)
      {
         pom.setPackaging(extension);
      }

      final Map<String, String> properties = artifact.getProperties();
      if (properties != null)
      {
         pom.getProperties().putAll(properties);
      }

      return pom;
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

      final CollectRequest collectRequest = newCollectRequest(session, project);
      return dependencyCollector.collectDependencies(session, collectRequest);
   }

   private static CollectRequest newCollectRequest(final RepositorySystemSession session, final MavenProject project)
   {
      ArtifactTypeRegistry stereotypes = session.getArtifactTypeRegistry();

      CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRootArtifact(RepositoryUtils.toArtifact(project.getArtifact()));
      collectRequest.setRequestContext("project");
      collectRequest.setRepositories(project.getRemoteProjectRepositories());

      if (project.getDependencyArtifacts() == null)
      {
         for (Dependency dependency : project.getDependencies())
         {
            if (StringUtils.isEmpty(dependency.getGroupId()) || StringUtils.isEmpty(dependency.getArtifactId())
               || StringUtils.isEmpty(dependency.getVersion()))
            {
               // guard against case where best-effort resolution for invalid models is requested
               continue;
            }
            collectRequest.addDependency(RepositoryUtils.toDependency(dependency, stereotypes));
         }
      }
      else
      {
         Map<String, Dependency> dependencies = new HashMap<String, Dependency>();
         for (Dependency dependency : project.getDependencies())
         {
            String classifier = dependency.getClassifier();
            if (classifier == null)
            {
               ArtifactType type = stereotypes.get(dependency.getType());
               if (type != null)
               {
                  classifier = type.getClassifier();
               }
            }
            String key = ArtifactIdUtils.toVersionlessId(dependency.getGroupId(), dependency.getArtifactId(),
               dependency.getType(), classifier);
            dependencies.put(key, dependency);
         }
         for (Artifact artifact : project.getDependencyArtifacts())
         {
            String key = artifact.getDependencyConflictId();
            Dependency dependency = dependencies.get(key);
            Collection<Exclusion> exclusions = dependency != null ? dependency.getExclusions() : null;
            org.eclipse.aether.graph.Dependency dep = RepositoryUtils.toDependency(artifact, exclusions);
            if (!JavaScopes.SYSTEM.equals(dep.getScope()) && dep.getArtifact().getFile() != null)
            {
               // enable re-resolution
               org.eclipse.aether.artifact.Artifact art = dep.getArtifact();
               art = art.setFile(null).setVersion(art.getBaseVersion());
               dep = dep.setArtifact(art);
            }
            collectRequest.addDependency(dep);
         }
      }

      DependencyManagement depMngt = project.getDependencyManagement();
      if (depMngt != null)
      {
         for (Dependency dependency : depMngt.getDependencies())
         {
            collectRequest.addManagedDependency(RepositoryUtils.toDependency(dependency, stereotypes));
         }
      }

      return collectRequest;
   }

   @Override
   @After
   public void tearDown() throws Exception
   {
      buildContext.setSession(null);
      super.tearDown();
   }

   private static void print(PrintStream out, Stack<DependencyNode> parentNodes, DependencyNode node, int level)
   {
      StringBuilder sb = new StringBuilder();
      if (level > 0)
      {
         for (int i = 0; i < level - 1; i++)
         {
            sb.append("|  ");
         }
         sb.append("+- ");
      }

      org.eclipse.aether.artifact.Artifact artifact = node.getArtifact();
      if (artifact == null)
      {
         sb.append("(null)");
      }
      else
      {
         sb.append(artifact.getGroupId());
         sb.append(':');
         sb.append(artifact.getArtifactId());
         sb.append(':');
         sb.append(artifact.getExtension());
         sb.append(':');
         sb.append(artifact.getVersion());
      }

      org.eclipse.aether.graph.Dependency dependency = node.getDependency();
      if (dependency != null)
      {
         sb.append(':');
         sb.append(dependency.getScope());
         if (dependency.isOptional())
         {
            sb.append(":optional");
         }
      }

      out.print(sb);
      level++;

      if (parentNodes.contains(node))
      {
         out.println(" (cyclic)");
      }
      else
      {
         out.println();

         parentNodes.push(node);
         for (DependencyNode dependencyNode : node.getChildren())
         {
            print(out, parentNodes, dependencyNode, level);
         }
         parentNodes.pop();
      }
   }

}
