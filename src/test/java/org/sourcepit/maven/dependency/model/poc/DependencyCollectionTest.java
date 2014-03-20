/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import static org.sourcepit.common.utils.io.IO.cpIn;
import static org.sourcepit.common.utils.io.IO.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.util.artifact.ArtifactIdUtils;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
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

   @Test
   public void testFoo() throws Exception
   {
      List<Model> models = parsePoms("DependencyCollectionTest/foo.txt");
      for (int i = 1; i < models.size(); i++)
      {
         repositoryFacade.deploy(models.get(i));
      }

      final File projectFile = getWs().newFile("pom.xml");
      modelWriter.write(projectFile, null, models.get(0));

      final MavenProject project = buildProject(projectFile).getProject();

      RepositorySystemSession session = buildContext.getRepositorySession();

      CollectResult collectResult = collectDependencies(session, project);

      print(collectResult.getRoot(), 0);
   }

   private List<Model> parsePoms(String res) throws IOException
   {
      List<DependencyNode> graphs = parseGraps(res);
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

      return pom;
   }

   private CollectResult collectDependencies(final RepositorySystemSession session, final MavenProject project)
      throws DependencyCollectionException
   {
      CollectRequest collectRequest = newCollectRequest(session, project);
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

   private static void print(DependencyNode node, int level)
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

      for (DependencyNode dependencyNode : node.getChildren())
      {
         print(dependencyNode, level);
      }
   }

   private List<DependencyNode> parseGraps(String resource) throws IOException
   {
      List<String> graphs = read(new Read.FromStream<List<String>>()
      {
         @Override
         public List<String> read(InputStream inputStream) throws Exception
         {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            List<String> segments = new ArrayList<String>();

            StringBuilder sb = null;

            for (String ln = r.readLine(); ln != null; ln = r.readLine())
            {
               if (ln.isEmpty())
               {
                  if (sb != null)
                  {
                     segments.add(sb.toString());
                     sb = null;
                  }
               }
               else
               {
                  if (sb == null)
                  {
                     sb = new StringBuilder();
                  }
                  sb.append(ln);
                  sb.append('\n');
               }
            }

            if (sb != null)
            {
               segments.add(sb.toString());
               sb = null;
            }

            return segments;
         }
      }, cpIn(getClass().getClassLoader(), resource));

      final List<DependencyNode> nodes = new ArrayList<DependencyNode>(graphs.size());
      for (String grap : graphs)
      {
         nodes.add(new DependencyGraphParser().parseLiteral(grap));
      }

      return nodes;
   }
}
