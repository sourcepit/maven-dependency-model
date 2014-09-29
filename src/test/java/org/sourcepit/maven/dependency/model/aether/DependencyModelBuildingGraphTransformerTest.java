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

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.common.maven.artifact.MavenArtifactUtils;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.maven.dependency.model.ArtifactAttachment;

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

      private Set<Artifact> referencedArtifacts = new HashSet<Artifact>();

      @Override
      public Set<ArtifactAttachment> artifact(Artifact artifact, boolean referenced)
      {
         if (referenced)
         {
            referencedArtifacts.add(artifact);
         }
         return null;
      }

      @Override
      public boolean startDependencyTree(Artifact artifact)
      {
         final boolean referenced = referencedArtifacts.contains(artifact);
         if (referenced)
         {
            out.println("+- " + artifact);
         }
         else
         {
            out.println("+- " + artifact + " (not referenced)");
         }
         return true;
      }

      @Override
      public void startDependencyNode(DependencyNode node, String effectiveScope, boolean optional, boolean selected,
         DependencyNode shadowedNode, DependencyNode cycleNode, boolean cycleWithTrees)
      {
         depth++;

         final StringBuilder sb = new StringBuilder();
         for (int i = 0; i < depth; i++)
         {
            sb.append("|  ");
         }
         sb.append("+- ");

         final Artifact artifact = node.getDependency().getArtifact();
         final ArtifactKey effectiveKey = MavenArtifactUtils.toArtifactKey(artifact);
         if (shadowedNode == null)
         {
            sb.append(effectiveKey);
            appendScope(sb, effectiveScope, node.getDependency().getScope(), optional, node.getDependency()
               .isOptional());
         }
         else
         {
            final ArtifactKey originKey = MavenArtifactUtils.toArtifactKey(shadowedNode.getDependency().getArtifact());
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

         if (cycleNode != null)
         {
            if (cycleWithTrees)
            {
               sb.append(" (cycle with root)");
            }
            else
            {
               sb.append(" (cycle)");
            }
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
