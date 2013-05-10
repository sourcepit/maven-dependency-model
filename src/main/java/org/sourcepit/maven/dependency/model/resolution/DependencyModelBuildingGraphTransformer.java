/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.resolution;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.util.MavenModelUtils;

public class DependencyModelBuildingGraphTransformer implements DependencyGraphTransformer
{
   private final class DirectReferenceDetectingDependencyVisitor extends AbstractDependencyVisitor
   {
      private boolean foundDirectReference = false;

      public DirectReferenceDetectingDependencyVisitor()
      {
         super(true);
      }

      public boolean isFoundDirectReference()
      {
         return foundDirectReference;
      }

      @Override
      protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
      {
         if (referencedNodes.contains(node))
         {
            foundDirectReference = true;
         }
         return !foundDirectReference;
      }

      @Override
      protected boolean onVisitLeave(DependencyNode parent, DependencyNode node)
      {
         return !foundDirectReference;
      }
   }

   private final boolean computeTreePerArtifact;

   private final boolean scopeTest;

   private final DependencyGraphTransformer transformer;

   private final NearestDependencyNodeChooser nodeChooser;

   private final DependencyModelHandler handler;

   private final Stack<String> parentScopes = new Stack<String>();
   private final Stack<Boolean> parentOptionals = new Stack<Boolean>();

   private final Map<DependencyNode, String> scopeMask = new HashMap<DependencyNode, String>();

   private final Set<DependencyNode> referencedNodes = new HashSet<DependencyNode>();

   private final Set<ArtifactKey> referencedAritfact = new HashSet<ArtifactKey>();

   public DependencyModelBuildingGraphTransformer(DependencyModelHandler handler, boolean computeTreePerArtifact,
      boolean scopeTest)
   {
      this.handler = handler;
      this.computeTreePerArtifact = computeTreePerArtifact;
      this.scopeTest = scopeTest;

      nodeChooser = new NearestDependencyNodeChooser();

      transformer = new ChainedDependencyGraphTransformer(new DependencyNode2AdapterTransformer(true),
         new HideDuplicatedSiblings(), new ApplyScopeAndOptional(), new VersionConflictResolver(nodeChooser),
         new HideReplacedNodes());
   }

   @Override
   public DependencyNode transformGraph(DependencyNode graph, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      handler.startDependencyModel();

      if (!computeTreePerArtifact)
      {
         graph = transformer.transformGraph(graph, context);
      }

      final List<DependencyNode> roots;
      if (graph.getDependency() == null)
      {
         roots = graph.getChildren();
      }
      else
      {
         roots = Collections.singletonList(graph);
      }
      traverseTrees(roots, context);

      handler.endDependencyModel();

      pruneUnreferenced(graph);

      return graph;
   }

   private void pruneUnreferenced(DependencyNode graph)
   {
      graph.accept(new AbstractDependencyVisitor(false)
      {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            for (Iterator<DependencyNode> it = node.getChildren().iterator(); it.hasNext();)
            {
               final DependencyNode childNode = (DependencyNode) it.next();
               if (!isReferenced(childNode))
               {
                  it.remove();
               }
            }

            return super.onVisitEnter(parent, node);
         }

         private boolean isReferenced(DependencyNode childNode)
         {
            Object object = childNode.getData().get("referenced");
            return object == null || ((Boolean) object).booleanValue();
         }
      });
   }

   private Stack<DependencyNode> nodeStack = new Stack<DependencyNode>();

   private void traverseTrees(List<DependencyNode> nodes, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      for (DependencyNode node : nodes)
      {
         if (nodeStack.contains(node))
         {
            final StringBuilder sb = new StringBuilder();
            for (DependencyNode n : nodeStack)
            {
               sb.append(n);
               sb.append(" -> ");
            }
            sb.append(node);

            throw new IllegalStateException("Cyclic dependencies " + sb);
         }

         nodeStack.push(node);

         traverseTrees(node.getChildren(), context);
         traverceTree(node, node.getDependency().getArtifact(), computeTreePerArtifact, context);

         nodeStack.pop();
      }
   }

   private DependencyNode currentRootNode;

   private Set<DependencyNode> selected = new HashSet<DependencyNode>();

   private Set<ArtifactKey> visited = new HashSet<ArtifactKey>();

   private void traverceTree(DependencyNode rootNode, Artifact rootArtifact, boolean computeTreePerArtifact,
      DependencyGraphTransformationContext context) throws RepositoryException
   {
      final ArtifactKey artifactKey = MavenModelUtils.toArtifactKey(rootArtifact);
      if (visited.add(artifactKey))
      {
         selected.clear();

         if (computeTreePerArtifact)
         {
            rootNode = transformer.transformGraph(rootNode, context);
         }
         else
         {
            new ResetVisibility().transformGraph(rootNode, context);
            new HideDuplicatedSiblings().transformGraph(rootNode, context);
            new ApplyScopeAndOptional().transformGraph(rootNode, context);
            new HideReplacedNodes().transformGraph(rootNode, context);
         }

         initScopeMask(rootNode);

         boolean referenced = isReferencedNode(rootNode);
         if (!referenced)
         {
            referenced = referencedAritfact.contains(artifactKey);
         }

         currentRootNode = rootNode;

         handler.startDependencyTree(rootArtifact, referenced);
         traverseNodesRecursive(rootNode.getChildren());
         handler.endDependencyTree(rootArtifact);

         currentRootNode = null;
      }
   }

   private void initScopeMask(DependencyNode root)
   {
      scopeMask.clear();

      final DependencyNode2 adapter = DependencyNode2Adapter.get(root);
      for (List<DependencyNode> conflictGroup : adapter.getConflictingNodeGroups())
      {
         final DependencyNode chosen = nodeChooser.choose(conflictGroup);
         for (DependencyNode node : conflictGroup)
         {
            if (node == chosen)
            {
               continue;
            }
            scopeMask.put(node, chosen.getDependency().getScope());
         }
      }
   }

   private boolean isReferencedNode(DependencyNode node)
   {
      if (this.referencedNodes.contains(node))
      {
         return true;
      }

      final DependencyNode2 adapter = DependencyNode2Adapter.get(node);

      boolean referenced = adapter.isVisible() && getRoot(node, adapter) != null;


      // && (scopeTest || !"test".equals(node.getDependency().getScope()));

      if (referenced && !scopeTest)
      {
         for (DependencyNode dependencyNode : nodeStack)
         {
            if ("test".equals(dependencyNode.getDependency().getScope()))
            {
               referenced = false;
               break;
            }
         }
      }


      if (referenced && adapter.getReplacement() != null)
      {
         ArtifactKey artifactKey1 = MavenModelUtils.toArtifactKey(node.getDependency().getArtifact());
         final DependencyNode replacement = getEffectiveNode(node);
         ArtifactKey artifactKey2 = MavenModelUtils.toArtifactKey(replacement.getDependency().getArtifact());
         if (!artifactKey1.equals(artifactKey2))
         {
            referenced = false;
         }
      }

      if (!referenced)
      {
         final DirectReferenceDetectingDependencyVisitor refDetector = new DirectReferenceDetectingDependencyVisitor();
         node.accept(refDetector);
         referenced = refDetector.isFoundDirectReference();
      }

      if (!referenced)
      {
         for (DependencyNode replaced : adapter.getReplaced())
         {
            DependencyNode2 replacedAdapter = DependencyNode2Adapter.get(replaced);

            Set<DependencyNode> parents = replacedAdapter.getParents();
            for (DependencyNode dependencyNode : parents)
            {
               if (DependencyNode2Adapter.get(dependencyNode).isVisible() && isReferencedNode(dependencyNode))
               {
                  // referenced = true;
                  break;
               }
            }
         }
      }

      if (referenced)
      {
         this.referencedNodes.add(node);
      }

      node.setData("referenced", Boolean.valueOf(referenced));

      return referenced;
   }

   private DependencyNode getRoot(DependencyNode node, DependencyNode2 adapter)
   {
      if (adapter.getParents().isEmpty())
      {
         return node;
      }

      for (DependencyNode parent : adapter.getParents())
      {
         final DependencyNode2 parentAdapter = DependencyNode2Adapter.get(parent);
         if (parentAdapter.getReplacement() != null)
         {
            continue;
         }

         final DependencyNode root = getRoot(parent, parentAdapter);
         if (root != null)
         {
            return root;
         }
      }

      return null;
   }

   private static boolean isRootOf(DependencyNode root, DependencyNode node, boolean followReplacements)
   {
      if (root.equals(node))
      {
         return true;
      }

      final DependencyNode2 adapter = DependencyNode2Adapter.get(node);

      for (DependencyNode parent : adapter.getParents())
      {
         final DependencyNode effectiveParent = getEffectiveNode(parent);

         if (!DependencyNode2Adapter.get(parent).isVisible() || !followReplacements && effectiveParent != parent)
         {
            continue;
         }

         if (isRootOf(root, effectiveParent, followReplacements))
         {
            return true;
         }
      }

      return false;
   }

   private void traverseNodesRecursive(List<DependencyNode> children)
   {
      for (DependencyNode child : children)
      {
         traverseNodeRecursive(child);
      }
   }

   private final Stack<Boolean> parentSelected = new Stack<Boolean>();

   private void traverseNodeRecursive(DependencyNode node)
   {
      final int depth = parentSelected.size();

      final boolean optional = isOptional(node);

      final DependencyNode effectiveNode = getEffectiveNode(node);

      final String scope = getEffectiveScope(node);

      final boolean selected = isSelected(node, effectiveNode, scope, depth);

      parentScopes.push(scope);
      parentOptionals.push(Boolean.valueOf(optional));
      parentSelected.push(Boolean.valueOf(selected));

      handler.startDependencyNode(effectiveNode, scope, optional, selected, effectiveNode == node ? null : node);
      traverseNodesRecursive(effectiveNode.getChildren());
      handler.endDependencyNode(effectiveNode);

      parentSelected.pop();
      parentOptionals.pop();
      parentScopes.pop();
   }

   private boolean isSelected(DependencyNode node, final DependencyNode effectiveNode, String effectiveScope,
      final int depth)
   {
      if (selected.contains(effectiveNode))
      {
         return false;
      }

      boolean active = true;
      if (depth > 0)
      {
         if (!parentSelected.peek().booleanValue())
         {
            active = false;
         }
         else if (!DependencyNode2Adapter.get(node).isVisible())
         {
            active = false;
         }
         else if (getCurrentOptional(node))
         {
            active = false;
         }
      }

      if (active && !scopeTest && "test".equals(effectiveScope))
      {
         active = false;
      }

      if (active && effectiveNode != node)
      {
         boolean isChosen = false;

         final Collection<List<DependencyNode>> conflictGroups = DependencyNode2Adapter.get(currentRootNode)
            .getConflictingNodeGroups();

         // is our node a chosen one (in our tree)?
         for (List<DependencyNode> conflictGroup : conflictGroups)
         {
            if (node == nodeChooser.choose(conflictGroup))
            {
               isChosen = true;
               break;
            }
         }

         if (!isChosen)
         {
            active = false;

            // our node is not a chosen one, but if no one else holds the ref to the effective node then we have to!
            // but only if the effective node can't be located in our tree...
            if (!isRootOf(currentRootNode, effectiveNode, false))
            {
               // if node is not in our tree, then check if it is referenced by another node in our tree
               final Set<DependencyNode> allConflictNodes = new HashSet<DependencyNode>();
               for (List<DependencyNode> conflictGroup : conflictGroups)
               {
                  allConflictNodes.addAll(conflictGroup);
               }
               allConflictNodes.remove(node);
               allConflictNodes.remove(effectiveNode);

               boolean refelsewhere = false;
               for (DependencyNode dependencyNode : allConflictNodes)
               {
                  if (getEffectiveNode(dependencyNode).equals(effectiveNode))
                  {
                     refelsewhere = true;
                     break;
                  }
               }

               if (!refelsewhere)
               {
                  active = true;
               }
            }
         }
      }

      if (active)
      {
         this.selected.add(effectiveNode);

         if (active)
         {
            Artifact referencedArtifact = effectiveNode.getDependency().getArtifact();
            referencedAritfact.add(MavenModelUtils.toArtifactKey(referencedArtifact));
         }


      }
      return active;
   }

   private boolean isOptional(DependencyNode node)
   {
      boolean optional = getCurrentOptional(node);
      if (!parentOptionals.isEmpty())
      {
         final boolean parentOptional = parentOptionals.peek().booleanValue();
         optional = parentOptional || optional;
      }
      return optional;
   }

   private String getEffectiveScope(DependencyNode node)
   {
      String scope = getCurrentScope(node);
      if (!parentScopes.isEmpty())
      {
         final String parentScope = parentScopes.peek();
         scope = DependencyUtils.getEffectiveScope(parentScope, scope, false);
      }
      return scope;
   }

   private String getCurrentScope(DependencyNode node)
   {
      String scope = scopeMask.get(node);
      if (scope == null)
      {
         scope = node.getDependency().getScope();
      }
      return scope;
   }

   private boolean getCurrentOptional(DependencyNode node)
   {
      return node.getDependency().isOptional();
   }

   private static DependencyNode getEffectiveNode(DependencyNode node)
   {
      final DependencyNode replacement = DependencyNode2Adapter.get(node).getReplacement();
      if (replacement == null)
      {
         return node;
      }
      return getEffectiveNode(replacement);
   }
}
