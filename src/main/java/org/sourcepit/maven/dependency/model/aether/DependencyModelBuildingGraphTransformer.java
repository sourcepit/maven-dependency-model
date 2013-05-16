/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import static org.sourcepit.common.maven.model.util.MavenModelUtils.toArtifactKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.VersionConflictKey;
import org.sourcepit.common.maven.model.util.MavenModelUtils;
import org.sourcepit.maven.dependency.model.ArtifactAttachment;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class DependencyModelBuildingGraphTransformer implements DependencyGraphTransformer
{
   private final boolean computeTreePerArtifact;

   private final boolean scopeTest;

   private final DependencyGraphTransformer transformer;

   private final NearestDependencyNodeChooser nodeChooser;

   private final DependencyModelHandler handler;

   private final Stack<String> parentScopes = new Stack<String>();
   private final Stack<Boolean> parentOptionals = new Stack<Boolean>();

   private final Map<DependencyNode, String> scopeMask = new HashMap<DependencyNode, String>();

   private Set<ArtifactKey> referencedArtifacts;

   private final ArtifactFactory artifactFactory;

   public DependencyModelBuildingGraphTransformer(ArtifactFactory artifactFactory, DependencyModelHandler handler,
      boolean computeTreePerArtifact, boolean scopeTest)
   {
      this.artifactFactory = artifactFactory;
      this.handler = handler;
      this.computeTreePerArtifact = computeTreePerArtifact;
      this.scopeTest = scopeTest;

      nodeChooser = new NearestDependencyNodeChooser();

      transformer = new ChainedDependencyGraphTransformer(new DependencyNode2AdapterTransformer(true),
         new HideDuplicatedSiblings(), new ApplyScopeAndOptional(), new VersionConflictResolver(nodeChooser),
         new HideReplacedNodes());
   }

   final Multimap<ArtifactKey, DependencyNode> keyToNodes = LinkedHashMultimap.create();

   @Override
   public DependencyNode transformGraph(DependencyNode graph, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      handler.startDependencyModel();

      if (!computeTreePerArtifact)
      {
         graph = transformer.transformGraph(graph, context);
      }

      referencedArtifacts = new HashSet<ArtifactKey>();
      computeReferencedArtifacts(graph, keyToNodes, referencedArtifacts);

      handleArtifacts(keyToNodes);

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

   private void handleArtifacts(final Multimap<ArtifactKey, DependencyNode> keyToNodes)
   {
      for (final ArtifactKey artifactKey : keyToNodes.keySet())
      {
         final Collection<DependencyNode> nodes = keyToNodes.get(artifactKey);
         final DependencyNode node = nodes.iterator().next();
         final Artifact artifact = node.getDependency().getArtifact();
         final boolean referenced = referencedArtifacts.contains(artifactKey);
         handleArtifacts(keyToNodes, nodes, artifact, referenced);
      }
   }

   private void handleArtifacts(Multimap<ArtifactKey, DependencyNode> keyToNodes, Collection<DependencyNode> nodes,
      Artifact artifact, boolean referenced)
   {
      final Set<ArtifactAttachment> attachments = handler.artifact(artifact, referenced);
      if (attachments != null)
      {
         for (ArtifactAttachment attachment : attachments)
         {
            Artifact attachedArtifact = artifactFactory.createArtifact(artifact, attachment.getClassifier(),
               attachment.getType());

            final ArtifactKey attachmentKey = MavenModelUtils.toArtifactKey(attachedArtifact);
            if (keyToNodes.containsKey(attachmentKey))
            {
               attachedArtifact = keyToNodes.get(attachmentKey).iterator().next().getDependency().getArtifact();
            }
            else
            {
               handleArtifacts(keyToNodes, nodes, attachedArtifact, referenced);
            }

            final String dataKey = attachment.isRequired() ? "requiredAttachments" : "optionalAttachments";
            for (DependencyNode node : nodes)
            {
               @SuppressWarnings("unchecked")
               Set<Artifact> attachedArtifacts = (Set<Artifact>) node.getData().get(dataKey);
               if (attachedArtifacts == null)
               {
                  attachedArtifacts = new LinkedHashSet<Artifact>();
                  node.setData(dataKey, attachedArtifacts);
               }
               attachedArtifacts.add(attachedArtifact);
            }
         }
      }
   }

   private void computeReferencedArtifacts(DependencyNode graph, final Multimap<ArtifactKey, DependencyNode> allNodes,
      final Set<ArtifactKey> referencedArtifacts)
   {
      graph.accept(new AbstractDependencyVisitor(false)
      {
         Stack<String> currentScope = new Stack<String>();

         Stack<Boolean> currentReplaced = new Stack<Boolean>();

         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            final String scope = getEffectivScope(node);

            final DependencyNode2 adapter = DependencyNode2Adapter.get(node);

            boolean replaced = adapter != null && adapter.getReplacement() != null;
            if (replaced)
            {
               final DependencyNode effectiveNode = getEffectiveNode(node);
               if (scopeTest || !"test".equals(scope))
               {
                  final Artifact artifact = effectiveNode.getDependency().getArtifact();
                  final ArtifactKey artifactKey = MavenModelUtils.toArtifactKey(artifact);
                  allNodes.put(artifactKey, effectiveNode);
                  addReferenced(allNodes, referencedArtifacts, parent, effectiveNode, artifactKey);
               }
            }

            if (!currentReplaced.isEmpty())
            {
               replaced = currentReplaced.peek().booleanValue() || replaced;
            }
            currentReplaced.push(Boolean.valueOf(replaced));

            Dependency dependency = node.getDependency();
            if (dependency != null)
            {
               final Artifact artifact = dependency.getArtifact();
               final ArtifactKey artifactKey = MavenModelUtils.toArtifactKey(artifact);
               allNodes.put(artifactKey, node);

               if (!replaced && (scopeTest || !"test".equals(scope)))
               {
                  addReferenced(allNodes, referencedArtifacts, parent, node, artifactKey);
               }
            }

            return super.onVisitEnter(parent, node);
         }

         private void addReferenced(final Multimap<ArtifactKey, DependencyNode> allNodes,
            final Set<ArtifactKey> referencedArtifacts, DependencyNode parent, DependencyNode node,
            ArtifactKey artifactKey)
         {
            boolean referenced = true;
            if (parent != null)
            {
               final Set<VersionConflictKey> keys1 = new DependencyNode2Adapter(parent).getConflictKeys();
               final Set<VersionConflictKey> keys2 = new DependencyNode2Adapter(node).getConflictKeys();
               if (DependencyUtils.isConflicting(keys1, keys2))
               {
                  referenced = false;
               }
               else
               {
                  final Dependency dependency = parent.getDependency();
                  if (dependency != null)
                  {
                     referenced = referencedArtifacts.contains(toArtifactKey(dependency.getArtifact()));
                  }
               }
            }

            if (referenced)
            {
               referencedArtifacts.add(artifactKey);
            }
         }


         private String getEffectivScope(DependencyNode node)
         {
            String scope = getNodeScope(node);
            if (!currentScope.isEmpty())
            {
               scope = "test".equals(currentScope.peek()) ? "test" : scope;
            }
            currentScope.push(scope);
            return scope;
         }


         @Override
         protected boolean onVisitLeave(DependencyNode parent, DependencyNode node)
         {
            super.onVisitLeave(parent, node);
            currentScope.pop();
            currentReplaced.pop();
            return true;
         }

         private String getNodeScope(DependencyNode node)
         {
            final Dependency dependency = node.getDependency();
            return dependency == null ? "compile" : dependency.getScope();
         }
      });
   }

   private void pruneUnreferenced(DependencyNode graph)
   {
      graph.accept(new AbstractDependencyVisitor(false)
      {
         @Override
         protected boolean onVisitLeave(DependencyNode parent, DependencyNode node)
         {
            for (Iterator<DependencyNode> it = node.getChildren().iterator(); it.hasNext();)
            {
               final DependencyNode childNode = (DependencyNode) it.next();
               if (!isReferenced(childNode))
               {
                  it.remove();
               }
            }
            return super.onVisitLeave(parent, node);
         }

         private boolean isReferenced(DependencyNode node)
         {
            Dependency dependency = node.getDependency();
            if (dependency != null)
            {
               return referencedArtifacts.contains(MavenModelUtils.toArtifactKey(dependency.getArtifact()));
            }
            return true;
         }
      });
   }

   private Stack<DependencyNode> treeStack = new Stack<DependencyNode>();

   private void traverseTrees(List<DependencyNode> nodes, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      for (DependencyNode node : nodes)
      {
         if (treeStack.contains(node))
         {
            final StringBuilder sb = new StringBuilder();
            for (DependencyNode n : treeStack)
            {
               sb.append(n);
               sb.append(" -> ");
            }
            sb.append(node);

            throw new IllegalStateException("Cyclic dependencies " + sb);
         }

         treeStack.push(node);

         traverseTrees(node.getChildren(), context);
         traverceTree(node, node.getDependency().getArtifact(), computeTreePerArtifact, context);

         treeStack.pop();
      }
   }

   private DependencyNode currentRootNode;

   private Set<DependencyNode> selected = new HashSet<DependencyNode>();

   private Set<ArtifactKey> visited = new HashSet<ArtifactKey>();

   private final Stack<DependencyNode> nodeStack = new Stack<DependencyNode>();

   private void traverceTree(DependencyNode rootNode, Artifact rootArtifact, boolean computeTreePerArtifact,
      DependencyGraphTransformationContext context) throws RepositoryException
   {
      final ArtifactKey artifactKey = MavenModelUtils.toArtifactKey(rootArtifact);
      if (visited.add(artifactKey))
      {
         selected.clear();

         // if same node exists with different scope then test use it, otherwise we'll lose dependencies through
         // ScopeChildDependenciesErasure
         if (!scopeTest && "test".equals(rootNode.getDependency().getScope()))
         {
            for (DependencyNode dependencyNode : keyToNodes.get(artifactKey))
            {
               if (!"test".equals(dependencyNode.getDependency().getScope()))
               {
                  rootNode = dependencyNode;
                  rootArtifact = dependencyNode.getDependency().getArtifact();
                  break;
               }
            }
         }

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

         currentRootNode = rootNode;
         nodeStack.push(rootNode);

         if (handler.startDependencyTree(rootArtifact))
         {
            traverseNodesRecursive(rootNode.getChildren());
         }
         handler.endDependencyTree(rootArtifact);

         currentRootNode = null;
         nodeStack.clear();
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

      final boolean cycle = nodeStack.contains(effectiveNode);

      final String scope = getEffectiveScope(node);

      final boolean selected = !cycle && isSelected(node, effectiveNode, scope, depth);

      nodeStack.push(effectiveNode);
      parentScopes.push(scope);
      parentOptionals.push(Boolean.valueOf(optional));
      parentSelected.push(Boolean.valueOf(selected));

      handler.startDependencyNode(effectiveNode, scope, optional, selected, effectiveNode == node ? null : node);
      if (!cycle)
      {
         traverseNodesRecursive(effectiveNode.getChildren());
      }
      handler.endDependencyNode(effectiveNode);

      parentSelected.pop();
      parentOptionals.pop();
      parentScopes.pop();
      nodeStack.pop();
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
      final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
      final DependencyNode replacement = adapter == null ? null : adapter.getReplacement();
      if (replacement == null)
      {
         return node;
      }
      return getEffectiveNode(replacement);
   }
}
