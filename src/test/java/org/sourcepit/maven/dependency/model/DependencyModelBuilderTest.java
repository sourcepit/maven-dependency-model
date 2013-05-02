/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.eclipse.emf.common.util.EList;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.util.graph.DefaultDependencyNode;
import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.maven.model.MavenDependency;
import org.sourcepit.common.maven.model.MavenModelFactory;
import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.common.maven.model.util.MavenModelUtils;

public class DependencyModelBuilderTest extends AbstractDependencyModelBuildingTest
{
   @Test
   public void testGetDependencyModel() throws Exception
   {
      DependencyModelBuilder builder = new DependencyModelBuilder();
      assertNull(builder.getDependencyModel());

      builder.startDependencyModel();

      builder.endDependencyModel();

      DependencyModel model = builder.getDependencyModel();
      assertNotNull(model);
      assertSame(model, builder.getDependencyModel());
   }

   @Test
   public void testArtifactsAreUnique()
   {
      DependencyModelBuilder builder = new DependencyModelBuilder();
      assertNull(builder.getDependencyModel());

      builder.startDependencyModel();

      String id = "a";

      Artifact a1 = newArtifact(id);
      builder.startDependencyTree(a1, true);
      builder.endDependencyTree(a1);

      Artifact a2 = newArtifact(id);
      builder.startDependencyTree(a2, true);
      builder.endDependencyTree(a2);

      builder.endDependencyModel();

      DependencyModel model = builder.getDependencyModel();
      assertNotNull(model);

      assertEquals(1, model.getArtifacts().size());
   }

   @Test
   public void testToDeclaredDependency() throws Exception
   {
      DefaultDependencyNode node = new DefaultDependencyNode(new Dependency(newArtifact("a"), "compile", true));

      MavenDependency declaredDependency = DependencyModelBuilder.toDeclaredDependency(node);
      assertEquals("a", declaredDependency.getGroupId());
      assertEquals("A", declaredDependency.getArtifactId());
      assertTrue(declaredDependency.isOptional());
      assertEquals("1", declaredDependency.getVersionConstraint());
      assertEquals(Scope.COMPILE, declaredDependency.getScope());


      GenericVersionScheme versionScheme = new GenericVersionScheme();
      node.setVersionConstraint(versionScheme.parseVersionConstraint("LATEST"));
      node.setVersion(versionScheme.parseVersion("LATEST"));

      declaredDependency = DependencyModelBuilder.toDeclaredDependency(node);
      assertEquals("a", declaredDependency.getGroupId());
      assertEquals("A", declaredDependency.getArtifactId());
      assertTrue(declaredDependency.isOptional());
      assertEquals("LATEST", declaredDependency.getVersionConstraint());
      assertEquals(Scope.COMPILE, declaredDependency.getScope());

      node.setPremanagedScope("test");
      node.setPremanagedVersion("66");

      declaredDependency = DependencyModelBuilder.toDeclaredDependency(node);
      assertEquals("a", declaredDependency.getGroupId());
      assertEquals("A", declaredDependency.getArtifactId());
      assertTrue(declaredDependency.isOptional());
      assertEquals("66", declaredDependency.getVersionConstraint());
      assertEquals(Scope.TEST, declaredDependency.getScope());
      
      node.setPremanagedScope("compile");
      node.setPremanagedVersion("1");

      declaredDependency = DependencyModelBuilder.toDeclaredDependency(node);
      assertEquals("a", declaredDependency.getGroupId());
      assertEquals("A", declaredDependency.getArtifactId());
      assertTrue(declaredDependency.isOptional());
      assertEquals("1", declaredDependency.getVersionConstraint());
      assertEquals(Scope.COMPILE, declaredDependency.getScope());
   }

   private Artifact newArtifact(String id)
   {
      DefaultArtifact mavenArtifact = new DefaultArtifact(id.toLowerCase(), id.toUpperCase(), "1", "compile", "jar",
         null, new DefaultArtifactHandler("jar"));
      return RepositoryUtils.toArtifact(mavenArtifact);
   }

   @Override
   protected DependencyModelHandler newPrinter(final PrintStream printStream)
   {
      return new DependencyModelBuilder()
      {
         @Override
         public void endDependencyModel()
         {
            super.endDependencyModel();


            DependencyModel dependencyModel = getDependencyModel();
            print(printStream, dependencyModel);
         }
      };
   }

   private void print(PrintStream out, DependencyModel model)
   {
      out.println("model");

      final EList<MavenArtifact> artifacts = model.getArtifacts();
      final EList<DependencyTree> trees = model.getDependencyTrees();

      final boolean hasArtifacts = !artifacts.isEmpty();
      final boolean hasDependencyTrees = !trees.isEmpty();

      if (hasArtifacts)
      {
         print(out, artifacts, hasDependencyTrees);
      }

      if (hasDependencyTrees)
      {
         print(out, trees);
      }
   }

   private void print(PrintStream out, final EList<DependencyTree> trees)
   {
      out.println("+- dependencyTrees");

      for (int i = 0; i < trees.size() - 1; i++)
      {
         DependencyTree tree = trees.get(i);
         out.print("   |- ");
         out.println(toString(tree));

         EList<org.sourcepit.maven.dependency.model.DependencyNode> dependencyNodes = tree.getDependencyNodes();
         for (org.sourcepit.maven.dependency.model.DependencyNode dependencyNode : dependencyNodes)
         {
            print(out, "   | ", dependencyNode, dependencyNodes);
         }
      }

      DependencyTree tree = trees.get(trees.size() - 1);
      out.print("   \\- ");
      out.println(toString(tree));

      EList<org.sourcepit.maven.dependency.model.DependencyNode> dependencyNodes = tree.getDependencyNodes();
      for (org.sourcepit.maven.dependency.model.DependencyNode dependencyNode : dependencyNodes)
      {
         print(out, "     ", dependencyNode, dependencyNodes);
      }
   }

   private void print(PrintStream out, String prefix, org.sourcepit.maven.dependency.model.DependencyNode node,
      List<org.sourcepit.maven.dependency.model.DependencyNode> siblings)
   {
      final boolean last = siblings.indexOf(node) == siblings.size() - 1;

      out.print(prefix);

      if (last)
      {
         out.print(" \\- ");
      }
      else
      {
         out.print(" |- ");
      }

      out.println(toString(node));

      final String appendix = last ? "   " : " | ";

      EList<org.sourcepit.maven.dependency.model.DependencyNode> children = node.getChildren();
      for (org.sourcepit.maven.dependency.model.DependencyNode dependencyNode : children)
      {
         print(out, prefix + appendix, dependencyNode, children);
      }
   }

   private String toString(org.sourcepit.maven.dependency.model.DependencyNode node)
   {
      final StringBuilder sb = new StringBuilder();
      final String is = toKey(node);
      sb.append(is);

      final String was = toKey(node.getDeclaredDependency());
      if (!was.equals(is))
      {
         sb.append(" (was ");
         sb.append(was);
         sb.append(')');
      }

      final MavenArtifact artifact = node.getArtifact();
      sb.append(" -> ");
      sb.append(toString(artifact));

      if (node.getConflictNode() != null || node.getConflictVersionConstraint() != null)
      {
         sb.append(" (conflicted)");
      }

      if (node.getManagedScope() != null)
      {
         sb.append(" (managed scope)");
      }

      if (node.getManagedVersionConstraint() != null)
      {
         sb.append(" (managed version)");
      }

      if (!node.isSelected())
      {
         sb.append(" (not selected)");
      }

      return sb.toString();
   }

   private String toKey(org.sourcepit.maven.dependency.model.DependencyNode node)
   {
      MavenArtifact artifact = MavenModelFactory.eINSTANCE.createMavenArtifact();
      artifact.setGroupId(node.getGroupId());
      artifact.setArtifactId(node.getArtifactId());
      artifact.setVersion(node.getEffectiveVersionConstraint());
      artifact.setClassifier(node.getClassifier());
      artifact.setType(node.getType());

      StringBuilder sb = new StringBuilder();
      sb.append(MavenModelUtils.toArtifactKey(artifact));
      sb.append(':');
      sb.append(node.getEffectiveScope());
      if (node.isOptional())
      {
         sb.append(':');
         sb.append('?');
      }

      return sb.toString();
   }

   private String toKey(MavenDependency dependency)
   {
      MavenArtifact artifact = MavenModelFactory.eINSTANCE.createMavenArtifact();
      artifact.setGroupId(dependency.getGroupId());
      artifact.setArtifactId(dependency.getArtifactId());
      artifact.setVersion(dependency.getVersionConstraint());
      artifact.setClassifier(dependency.getClassifier());
      artifact.setType(dependency.getType());

      StringBuilder sb = new StringBuilder();
      sb.append(MavenModelUtils.toArtifactKey(artifact));
      sb.append(':');
      sb.append(dependency.getScope());
      if (dependency.isOptional())
      {
         sb.append(':');
         sb.append('?');
      }

      return sb.toString();
   }

   private String toString(DependencyTree tree)
   {
      return toString(tree.getTargetArtifact());
   }

   private void print(PrintStream out, final EList<MavenArtifact> artifacts, final boolean hasDependencyTrees)
   {
      out.println("+- artifacts");

      for (int i = 0; i < artifacts.size() - 1; i++)
      {
         if (hasDependencyTrees)
         {
            out.print("|  |- ");
         }
         else
         {
            out.print("   |- ");
         }
         out.println(toString(artifacts.get(i)));
      }

      if (hasDependencyTrees)
      {
         out.print("|  \\- ");
      }
      else
      {
         out.print("   \\- ");
      }
      out.println(toString(artifacts.get(artifacts.size() - 1)));
   }

   private String toString(MavenArtifact artifact)
   {
      return artifact.getArtifactKey().toString();
   }
}
