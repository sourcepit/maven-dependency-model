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

import static org.apache.commons.io.IOUtils.copy;
import static org.junit.Assert.assertEquals;
import static org.sourcepit.common.utils.io.IO.buffIn;
import static org.sourcepit.common.utils.io.IO.cpIn;
import static org.sourcepit.common.utils.io.IO.read;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;

import javax.inject.Inject;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.sourcepit.common.maven.artifact.ArtifactFactory;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.common.utils.io.Read.FromStream;

public abstract class AbstractDependencyModelBuildingTest extends EmbeddedMavenEnvironmentTest
{
   @Inject
   private ArtifactFactory artifactFactory;

   @Rule
   public TestName name = new TestName();

   @Test
   public void testCycle1() throws Exception
   {
      test();
   }

   @Test
   public void testCycle2() throws Exception
   {
      test();
   }

   @Test
   public void testSimple() throws Exception
   {
      test();
   }

   @Test
   public void testSelected() throws Exception
   {
      test();
   }

   @Test
   public void testOptional1() throws Exception
   {
      test();
   }

   @Test
   public void testOptional2() throws Exception
   {
      test();
   }

   @Test
   public void testOptional3() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict1() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict2() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict3() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict4() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict5() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict6() throws Exception
   {
      test();
   }

   @Test
   public void testVersionConflict7() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope1() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope2() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope3() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope4() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope5() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope6() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope7() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope8() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope9() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope10() throws Exception
   {
      test();
   }

   @Test
   public void testEffectiveScope11() throws Exception
   {
      test();
   }

   private void test() throws Exception
   {
      DependencyNode graph;
      String actual;
      String expected;

      final String input = "TestCases/" + getTestName() + ".txt";

      final String prefix = getClass().getSimpleName() + "/" + getTestName();

      final String prefixCompile = prefix + "/compile";

      graph = parseDependencyGraph(input);
      actual = transformGraph(null, graph, false, false);
      expected = getStringContent(prefixCompile + "/Out.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(null, graph, true, false);
      expected = getStringContent(prefixCompile + "/OutPerArtifact.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(new DefaultArtifact("root", "ROOT", "jar", "1"), graph, false, false);
      expected = getStringContent(prefixCompile + "/OutWithRoot.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(new DefaultArtifact("root", "ROOT", "jar", "1"), graph, true, false);
      expected = getStringContent(prefixCompile + "/OutWithRootPerArtifact.txt");
      assertEquals(expected, actual);

      final String prefixTest = prefix + "/test";
      graph = parseDependencyGraph(input);
      actual = transformGraph(null, graph, false, true);
      expected = getStringContent(prefixTest + "/Out.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(null, graph, true, true);
      expected = getStringContent(prefixTest + "/OutPerArtifact.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(new DefaultArtifact("root", "ROOT", "jar", "1"), graph, false, true);
      expected = getStringContent(prefixTest + "/OutWithRoot.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(new DefaultArtifact("root", "ROOT", "jar", "1"), graph, true, true);
      expected = getStringContent(prefixTest + "/OutWithRootPerArtifact.txt");
      assertEquals(expected, actual);
   }

   protected String getTestName()
   {
      return name.getMethodName().substring("test".length());
   }

   private String getStringContent(String resource)
   {
      final FromStream<String> fromStream = new FromStream<String>()
      {
         @Override
         public String read(InputStream inputStream) throws Exception
         {
            final StringWriter writer = new StringWriter();
            copy(inputStream, writer, "UTF-8");
            return writer.toString();
         }
      };
      return read(fromStream, buffIn(cpIn(getClass().getClassLoader(), resource)));
   }

   private String transformGraph(Artifact root, DependencyNode graph, boolean computeTreePerArtifact, boolean scopeTest)
      throws RepositoryException
   {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(bytes);
      DependencyModelHandler printer = newPrinter(printStream);


      ReplaceRootNode transformer1 = null;
      if (root != null)
      {
         DefaultDependencyNode rootNode = new DefaultDependencyNode(new org.eclipse.aether.graph.Dependency(root,
            "compile"));
         rootNode.setRequestContext("project");

         transformer1 = new ReplaceRootNode(rootNode);
      }

      DependencyModelBuildingGraphTransformer transformer2 = new DependencyModelBuildingGraphTransformer(
         artifactFactory, printer, computeTreePerArtifact, scopeTest);

      ChainedDependencyGraphTransformer.newInstance(transformer1, transformer2).transformGraph(graph, null);

      return new String(bytes.toByteArray());
   }

   protected abstract DependencyModelHandler newPrinter(PrintStream printStream);

   private DependencyNode parseDependencyGraph(String resource) throws IOException
   {
      return new DependencyGraphParser().parse(resource);
   }

   @Override
   protected Environment newEnvironment()
   {
      return Environment.get("env-test.properties");
   }

}
