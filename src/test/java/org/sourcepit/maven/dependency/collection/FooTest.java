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

package org.sourcepit.maven.dependency.collection;

import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.addDependency;
import static org.sourcepit.maven.dependency.collection.CollectionTestHarness.newPom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.superpom.SuperPomProvider;
import org.apache.maven.plugin.LegacySupport;
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.util.graph.transformer.NoopDependencyGraphTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.sourcepit.common.maven.artifact.ArtifactFactory;
import org.sourcepit.common.maven.testing.ArtifactRepositoryFacade;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.maven.dependency.aether.SrcpitDependencyCollector;
import org.sourcepit.maven.dependency.model.poc.TestHarness;

import com.google.inject.Binder;

public class FooTest extends EmbeddedMavenEnvironmentTest
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
   protected void configureCustomBindings(Binder binder)
   {
      super.configureCustomBindings(binder);
      // avoid requests to central (that will fail on system behind proxies and without permission to get out)
      binder.bind(SuperPomProvider.class).to(FilterCentralFromSuperPom.class);
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
      Model x0y0 = newPom("x0y0");
      Model x1y0 = newPom("x1y0");
      Model x2y0 = newPom("x2y0");

      Model x0y1 = newPom("x0y1");
      Model x1y1 = newPom("x1y1");
      Model x2y1 = newPom("x2y1");

      Model x0y2 = newPom("x0y2");
      Model x1y2 = newPom("x1y2");
      Model x2y2 = newPom("x2y2");

      addDependency(x0y0, x0y1);
      addDependency(x0y1, x0y2);

      addDependency(x1y0, x1y1);
      addDependency(x1y1, x1y2);

      addDependency(x2y0, x2y1);
      addDependency(x2y1, x2y2);

      repositoryFacade.deploy(x0y0);
      repositoryFacade.deploy(x1y0);
      repositoryFacade.deploy(x2y0);

      repositoryFacade.deploy(x0y1);
      repositoryFacade.deploy(x1y1);
      repositoryFacade.deploy(x2y1);

      repositoryFacade.deploy(x0y2);
      repositoryFacade.deploy(x1y2);
      repositoryFacade.deploy(x2y2);

      List<Dependency> dependencies = new ArrayList<Dependency>();
      dependencies.add(CollectionTestHarness.toDependency(artifactFactory, x0y0));
      dependencies.add(CollectionTestHarness.toDependency(artifactFactory, x1y0));
      dependencies.add(CollectionTestHarness.toDependency(artifactFactory, x2y0).setScope("test"));

      CollectRequest request = new CollectRequest();
      request.addRepository(RepositoryUtils.toRepo(embeddedMaven.getRemoteRepository()));
      request.setDependencies(dependencies);

      final RepositorySystemSession session = new AbstractForwardingRepositorySystemSession()
      {
         @Override
         protected RepositorySystemSession getSession()
         {
            return buildContext.getRepositorySession();
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer()
         {
            return new NoopDependencyGraphTransformer();
         }
      };

      CollectResult result = srcpitDependencyCollector.collectDependencies(session, request);

      System.out.println(TestHarness.toString(result));
   }
}
