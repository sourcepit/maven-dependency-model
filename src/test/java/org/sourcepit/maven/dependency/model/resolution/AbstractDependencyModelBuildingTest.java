/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.resolution;

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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.DefaultDependencyNode;
import org.sonatype.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.common.utils.io.Read.FromStream;

public abstract class AbstractDependencyModelBuildingTest extends EmbeddedMavenEnvironmentTest
{
   @Rule
   public TestName name = new TestName();

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

   private void test() throws Exception
   {
      DependencyNode graph;
      String actual;
      String expected;

      final String input = "TestCases/" + getTestName() + ".txt";

      final String prefix = getClass().getSimpleName() + "/" + getTestName();

      graph = parseDependencyGraph(input);
      actual = transformGraph(null, graph, false);
      expected = getStringContent(prefix + "/Out.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(null, graph, true);
      expected = getStringContent(prefix + "/OutPerArtifact.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(new DefaultArtifact("root", "ROOT", "jar", "1"), graph, false);
      expected = getStringContent(prefix + "/OutWithRoot.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(input);
      actual = transformGraph(new DefaultArtifact("root", "ROOT", "jar", "1"), graph, true);
      expected = getStringContent(prefix + "/OutWithRootPerArtifact.txt");
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
      return read(fromStream, buffIn(cpIn(getClassLoader(), resource)));
   }

   private String transformGraph(Artifact root, DependencyNode graph, boolean computeTreePerArtifact)
      throws RepositoryException
   {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(bytes);
      DependencyModelHandler printer = newPrinter(printStream);


      ReplaceRootNode transformer1 = null;
      if (root != null)
      {
         DefaultDependencyNode rootNode = new DefaultDependencyNode();
         rootNode.setDependency(new org.sonatype.aether.graph.Dependency(root, "compile"));
         rootNode.setRequestContext("project");

         transformer1 = new ReplaceRootNode(rootNode);
      }

      DependencyModelBuildingGraphTransformer transformer2 = new DependencyModelBuildingGraphTransformer(printer,
         computeTreePerArtifact);

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
