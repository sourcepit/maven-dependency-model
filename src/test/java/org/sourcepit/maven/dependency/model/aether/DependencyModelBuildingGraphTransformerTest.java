/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import java.io.PrintStream;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.util.MavenModelUtils;
import org.sourcepit.maven.dependency.model.aether.DependencyModelHandler;

public class DependencyModelBuildingGraphTransformerTest extends AbstractDependencyModelBuildingTest
{
   @Override
   protected DependencyModelHandler newPrinter(PrintStream printStream)
   {
      return new DependencyModelPriner(printStream);
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
         final ArtifactKey effectiveKey = MavenModelUtils.toArtifactKey(artifact);
         if (shadowedNode == null)
         {
            sb.append(effectiveKey);
            appendScope(sb, effectiveScope, node.getDependency().getScope(), optional, node.getDependency()
               .isOptional());
         }
         else
         {
            final ArtifactKey originKey = MavenModelUtils.toArtifactKey(shadowedNode.getDependency().getArtifact());
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
