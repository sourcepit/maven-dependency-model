/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

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
import org.sourcepit.common.maven.model.util.MavenModelUtils;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.common.utils.io.Read.FromStream;

public class DependencyModelBuildingGraphTransformerTest extends EmbeddedMavenEnvironmentTest
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
   public void testOptional() throws Exception
   {
      test();
   }

   @Test
   public void testOptional2() throws Exception
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

   private void test() throws Exception
   {
      DependencyNode graph;
      String actual;
      String expected;

      final String prefix = getClass().getSimpleName() + "/" + getTestName();

      graph = parseDependencyGraph(prefix + "/In.txt");
      actual = transformGraph(null, graph, false);
      expected = getStringContent(prefix + "/Out.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(prefix + "/In.txt");
      actual = transformGraph(null, graph, true);
      expected = getStringContent(prefix + "/OutPerArtifact.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(prefix + "/In.txt");
      actual = transformGraph(new DefaultArtifact("root", "ROOT", "jar", "1"), graph, false);
      expected = getStringContent(prefix + "/OutWithRoot.txt");
      assertEquals(expected, actual);

      graph = parseDependencyGraph(prefix + "/In.txt");
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
      DependencyModelPriner printer = new DependencyModelPriner(new PrintStream(bytes));

      DependencyModelBuildingGraphTransformer strategy = new DependencyModelBuildingGraphTransformer(printer,
         computeTreePerArtifact, root);

      strategy.transformGraph(graph, null);

      return new String(bytes.toByteArray());
   }

   private DependencyNode parseDependencyGraph(String resource) throws IOException
   {
      return new DependencyGraphParser().parse(resource);
   }

   @Override
   protected Environment newEnvironment()
   {
      return Environment.get("env-test.properties");
   }

   private class DependencyModelPriner implements DependencyModelHandler
   {
      private final PrintStream out;

      public DependencyModelPriner(PrintStream out)
      {
         this.out = out;
      }

      private int depth = 0;

      @Override
      public void startDependencyModel()
      {
         out.println("model");
      }

      @Override
      public void startDependencyTree(Artifact artifact, boolean referenced)
      {
         if (referenced)
         {
            out.println("+- " + artifact);
         }
         else
         {
            out.println("+- " + artifact + " (not referenced)");
         }
      }

      @Override
      public void startDependencyNode(DependencyNode node, String effectiveScope, boolean optional, boolean selected,
         DependencyNode shadowedNode)
      {
         depth++;

         final StringBuilder sb = new StringBuilder();
         for (int i = 0; i < depth; i++)
         {
            sb.append("|  ");
         }
         sb.append("+- ");

         final Artifact artifact = node.getDependency().getArtifact();
         final String effectiveKey = MavenModelUtils.toArtifactKey(artifact);
         if (shadowedNode == null)
         {
            sb.append(effectiveKey);
            appendScope(sb, effectiveScope, node.getDependency().getScope(), optional, node.getDependency()
               .isOptional());
         }
         else
         {
            final String originKey = MavenModelUtils.toArtifactKey(shadowedNode.getDependency().getArtifact());
            sb.append(originKey);
            if (!effectiveKey.equals(originKey))
            {
               sb.append(" -> ");
               sb.append(effectiveKey);
            }
            appendScope(sb, effectiveScope, shadowedNode.getDependency().getScope(), optional, shadowedNode
               .getDependency().isOptional());
         }

         if (!selected)
         {
            sb.append(" (not selected)");
         }

         out.println(sb);
      }

      private void appendScope(final StringBuilder sb, String effectiveScope, String shadowedScope,
         boolean effeciveOptional, boolean shadowedOptional)
      {
         sb.append(" (");

         effectiveScope = effectiveScope == null ? "none" : effectiveScope;
         shadowedScope = shadowedScope == null ? "none" : shadowedScope;

         if (effectiveScope.equals(shadowedScope) && effeciveOptional == shadowedOptional)
         {
            sb.append(effectiveScope);
         }
         else
         {
            sb.append(shadowedScope);
            if (shadowedOptional)
            {
               sb.append("?");
            }
            sb.append(" -> ");
            sb.append(effectiveScope);
         }
         if (effeciveOptional)
         {
            sb.append("?");
         }
         sb.append(")");
      }

      @Override
      public void endDependencyNode(DependencyNode node)
      {
         depth--;
      }

      @Override
      public void endDependencyTree(Artifact artifact)
      {
         depth = 0;
      }

      @Override
      public void endDependencyModel()
      {
      }
   }

}
