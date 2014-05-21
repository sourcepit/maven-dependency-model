/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import static com.google.common.base.Preconditions.checkState;
import static org.sourcepit.common.maven.core.MavenCoreUtils.toArtifactKey;
import static org.sourcepit.common.maven.core.MavenCoreUtils.toMavenArtifact;
import static org.sourcepit.common.maven.core.MavenCoreUtils.toMavenDependecy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionConstraint;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.maven.model.MavenDependency;
import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.maven.dependency.model.ArtifactAttachment;
import org.sourcepit.maven.dependency.model.ArtifactAttachmentFactory;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyModelFactory;
import org.sourcepit.maven.dependency.model.DependencyTree;

public class DependencyModelBuilder implements DependencyModelHandler
{
   private final DependencyModelFactory dependencyModelFactory;
   private DependencyModel model;

   private final ArtifactAttachmentFactory attachmentFactory;

   public DependencyModelBuilder()
   {
      this(null);
   }

   public DependencyModelBuilder(ArtifactAttachmentFactory attachmentFactory)
   {
      this.dependencyModelFactory = DependencyModelFactory.eINSTANCE;
      this.attachmentFactory = attachmentFactory;
   }

   @Override
   public void startDependencyModel()
   {
      model = dependencyModelFactory.createDependencyModel();
   }

   private final Map<ArtifactKey, MavenArtifact> keyToArtifact = new HashMap<ArtifactKey, MavenArtifact>();

   private final Set<ArtifactKey> unreferencedArtifacts = new HashSet<ArtifactKey>();

   private DependencyTree currentDependencyTree;

   @Override
   public Set<ArtifactAttachment> artifact(Artifact artifact, boolean referenced)
   {
      if (referenced)
      {
         if (addArtifactUnique(artifact) && attachmentFactory != null)
         {
            return attachmentFactory.createAttachments(toArtifactKey(artifact));
         }
      }
      else
      {
         unreferencedArtifacts.add(toArtifactKey(artifact));
      }

      return null;
   }

   @Override
   public boolean startDependencyTree(Artifact artifact)
   {
      final MavenArtifact mavenArtifact = keyToArtifact.get(toArtifactKey(artifact));
      if (mavenArtifact != null)
      {
         final DependencyTree dependencyTree = dependencyModelFactory.createDependencyTree();
         dependencyTree.setArtifact(mavenArtifact);
         model.getDependencyTrees().add(dependencyTree);
         currentDependencyTree = dependencyTree;
      }
      return mavenArtifact != null;
   }

   private boolean addArtifactUnique(Artifact artifact)
   {
      final MavenArtifact mavenArtifact = toMavenArtifact(artifact);
      final ArtifactKey key = mavenArtifact.getArtifactKey();
      if (!keyToArtifact.containsKey(key))
      {
         model.getArtifacts().add(mavenArtifact);
         keyToArtifact.put(key, mavenArtifact);
         return true;
      }
      return false;
   }

   private Map<DependencyNode, org.sourcepit.maven.dependency.model.DependencyNode> unreplacedNodes = new HashMap<DependencyNode, org.sourcepit.maven.dependency.model.DependencyNode>();

   private Map<org.sourcepit.maven.dependency.model.DependencyNode, DependencyNode> replacedNodes = new HashMap<org.sourcepit.maven.dependency.model.DependencyNode, DependencyNode>();

   private Stack<org.sourcepit.maven.dependency.model.DependencyNode> dependencyNodeStack = new Stack<org.sourcepit.maven.dependency.model.DependencyNode>();

   private Map<DependencyNode, org.sourcepit.maven.dependency.model.DependencyNode> treeNodes = new HashMap<DependencyNode, org.sourcepit.maven.dependency.model.DependencyNode>();

   @Override
   public void startDependencyNode(DependencyNode effectiveNode, String inheritedScope, boolean optional,
      boolean selected, DependencyNode shadowedNode, DependencyNode cycleNode, boolean cycleWithTree)
   {
      final org.sourcepit.maven.dependency.model.DependencyNode node = createDependencyNode(effectiveNode,
         inheritedScope, optional, selected, shadowedNode);

      final ArtifactKey artifactKey = toArtifactKey(effectiveNode.getDependency().getArtifact());

      final MavenArtifact mavenArtifact = keyToArtifact.get(artifactKey);
      checkState(mavenArtifact != null || unreferencedArtifacts.contains(artifactKey), "Artifact %s unknown.",
         artifactKey);
      node.setArtifact(mavenArtifact);

      if (shadowedNode == null)
      {
         unreplacedNodes.put(effectiveNode, node);
      }
      else
      {
         replacedNodes.put(node, effectiveNode);
      }

      if (cycleNode != null)
      {
         if (cycleWithTree)
         {
            node.setSelected(false);
         }
         else
         {
            org.sourcepit.maven.dependency.model.DependencyNode target = treeNodes.get(cycleNode);
            checkState(target != null);
            node.setCycleNode(target);
         }
      }

      if (dependencyNodeStack.isEmpty())
      {
         currentDependencyTree.getDependencyNodes().add(node);
      }
      else
      {
         dependencyNodeStack.peek().getChildren().add(node);
      }

      if (!treeNodes.containsKey(effectiveNode))
      {
         treeNodes.put(effectiveNode, node);
      }

      dependencyNodeStack.push(node);
   }

   private org.sourcepit.maven.dependency.model.DependencyNode createDependencyNode(DependencyNode effectiveNode,
      String inheritedScope, boolean optional, boolean selected, DependencyNode shadowedNode)
   {
      final boolean shadowing = shadowedNode != null;

      final DependencyNode declaredNode = shadowing ? shadowedNode : effectiveNode;

      org.sourcepit.maven.dependency.model.DependencyNode node = dependencyModelFactory.createDependencyNode();
      node.setSelected(selected);
      node.setOptional(optional);
      node.setDeclaredDependency(toDeclaredDependency(declaredNode));

      final Scope scope = Scope.get(inheritedScope);
      if (scope != node.getDeclaredDependency().getScope())
      {
         node.setInheritedScope(scope);
      }

      final Scope managedScope = getManagedScope(effectiveNode);
      if (managedScope != null)
      {
         node.setManagedScope(managedScope);
      }

      final String managedVersionConstraint = getManagedVersionConstraint(effectiveNode);
      if (managedVersionConstraint != null)
      {
         node.setManagedVersionConstraint(managedVersionConstraint);
      }

      if (shadowing)
      {
         final VersionConstraint versionConstraint = effectiveNode.getVersionConstraint();
         if (versionConstraint != null)
         {
            node.setConflictVersionConstraint(versionConstraint.toString());
         }
      }

      return node;
   }

   private String getManagedVersionConstraint(DependencyNode node)
   {
      if (DependencyManagerUtils.getPremanagedVersion(node) != null)
      {
         return node.getVersionConstraint().toString();
      }
      return null;
   }

   private static Scope getManagedScope(DependencyNode node)
   {
      if (DependencyManagerUtils.getPremanagedScope(node) != null)
      {
         return Scope.get(node.getDependency().getScope());
      }
      return null;
   }

   public static MavenDependency toDeclaredDependency(DependencyNode node)
   {
      final MavenDependency declaredDep = toMavenDependecy(node.getDependency());

      final String declaredVersionConstraint = getDeclaredVersionConstraint(node);
      if (!declaredVersionConstraint.equals(declaredDep.getVersionConstraint()))
      {
         declaredDep.setVersionConstraint(declaredVersionConstraint);
      }

      final Scope declaredScope = getDeclaredScope(node);
      if (declaredScope != declaredDep.getScope())
      {
         declaredDep.setScope(declaredScope);
      }

      return declaredDep;
   }

   private static String getDeclaredVersionConstraint(DependencyNode node)
   {
      final String premanagedVersion = DependencyManagerUtils.getPremanagedVersion(node);
      if (premanagedVersion != null)
      {
         return premanagedVersion;
      }

      final VersionConstraint versionConstraint = node.getVersionConstraint();
      if (versionConstraint != null)
      {
         return versionConstraint.toString();
      }

      Version version = node.getVersion();
      if (version != null)
      {
         return version.toString();
      }

      String artifactVersion = node.getDependency().getArtifact().getVersion();
      if (artifactVersion != null)
      {
         return artifactVersion;
      }

      throw new IllegalStateException("Unable to determine declared version for node " + node);
   }

   private static Scope getDeclaredScope(DependencyNode node)
   {
      String declaredScope = DependencyManagerUtils.getPremanagedScope(node);
      if (declaredScope == null)
      {
         declaredScope = node.getDependency().getScope();
      }
      return Scope.get(declaredScope);
   }

   @Override
   public void endDependencyNode(DependencyNode node)
   {
      dependencyNodeStack.pop();
   }

   @Override
   public void endDependencyTree(Artifact artifact)
   {
      resolveConflictNodes();
      currentDependencyTree = null;
      treeNodes.clear();
   }

   private void resolveConflictNodes()
   {
      final List<org.sourcepit.maven.dependency.model.DependencyNode> resolved = new ArrayList<org.sourcepit.maven.dependency.model.DependencyNode>();

      for (Entry<org.sourcepit.maven.dependency.model.DependencyNode, DependencyNode> entry : replacedNodes.entrySet())
      {
         final org.sourcepit.maven.dependency.model.DependencyNode conflictNode = unreplacedNodes.get(entry.getValue());
         if (conflictNode != null)
         {
            final org.sourcepit.maven.dependency.model.DependencyNode replacedNode = entry.getKey();
            replacedNode.setConflictNode(conflictNode);
            resolved.add(replacedNode);
         }
      }

      for (org.sourcepit.maven.dependency.model.DependencyNode dependencyNode : resolved)
      {
         replacedNodes.remove(dependencyNode);
      }
   }

   @Override
   public void endDependencyModel()
   {
      resolveConflictNodes();
      // checkState(replacedNodes.isEmpty(), "Unable to find conflict nodes for nodes %s", replacedNodes);
   }

   public DependencyModel getDependencyModel()
   {
      return model;
   }

}
