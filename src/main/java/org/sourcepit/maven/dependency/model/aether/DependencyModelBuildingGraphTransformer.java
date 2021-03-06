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

import static org.sourcepit.common.maven.artifact.MavenArtifactUtils.toArtifactKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.sourcepit.common.maven.artifact.ArtifactFactory;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.VersionConflictKey;
import org.sourcepit.maven.dependency.model.ArtifactAttachment;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class DependencyModelBuildingGraphTransformer implements DependencyGraphTransformer {
   private final boolean computeTreePerArtifact;

   private final boolean scopeTest;

   private final DependencyGraphTransformer transformer;

   private final NearestDependencyNodeChooser nodeChooser;

   private final DependencyModelHandler handler;

   private Set<ArtifactKey> referencedArtifacts;

   private final ArtifactFactory artifactFactory;

   public DependencyModelBuildingGraphTransformer(ArtifactFactory artifactFactory, DependencyModelHandler handler,
      boolean computeTreePerArtifact, boolean scopeTest) {
      this.artifactFactory = artifactFactory;
      this.handler = handler;
      this.computeTreePerArtifact = computeTreePerArtifact;
      this.scopeTest = scopeTest;

      nodeChooser = new NearestDependencyNodeChooser(!computeTreePerArtifact);

      transformer = new ChainedDependencyGraphTransformer(new DependencyNode2AdapterTransformer(true),
         new HideDuplicatedSiblings(), new ApplyScopeAndOptional(), new VersionConflictResolver(nodeChooser));
   }

   final Multimap<ArtifactKey, DependencyNode> keyToNodes = LinkedHashMultimap.create();

   @Override
   public DependencyNode transformGraph(DependencyNode graph, DependencyGraphTransformationContext context)
      throws RepositoryException {
      handler.startDependencyModel();

      if (!computeTreePerArtifact) {
         graph = transformer.transformGraph(graph, context);
      }

      referencedArtifacts = new HashSet<ArtifactKey>();
      computeReferencedArtifacts(graph, keyToNodes, referencedArtifacts);

      handleArtifacts(keyToNodes);

      final List<DependencyNode> roots;
      if (graph.getDependency() == null) {
         roots = graph.getChildren();
      }
      else {
         roots = Collections.singletonList(graph);
      }
      traverseTrees(roots, context);

      handler.endDependencyModel();

      pruneUnreferenced(graph);

      return graph;
   }

   private void handleArtifacts(final Multimap<ArtifactKey, DependencyNode> keyToNodes) {
      for (final ArtifactKey artifactKey : keyToNodes.keySet()) {
         final Collection<DependencyNode> nodes = keyToNodes.get(artifactKey);
         final DependencyNode node = nodes.iterator().next();
         final Artifact artifact = node.getDependency().getArtifact();
         final boolean referenced = referencedArtifacts.contains(artifactKey);
         handleArtifacts(keyToNodes, nodes, artifact, referenced);
      }
   }

   private void handleArtifacts(Multimap<ArtifactKey, DependencyNode> keyToNodes, Collection<DependencyNode> nodes,
      Artifact artifact, boolean referenced) {
      final Set<ArtifactAttachment> attachments = handler.artifact(artifact, referenced);
      if (attachments != null) {
         for (ArtifactAttachment attachment : attachments) {
            Artifact attachedArtifact = artifactFactory.createArtifact(artifact, attachment.getClassifier(),
               attachment.getType());

            final ArtifactKey attachmentKey = toArtifactKey(attachedArtifact);
            if (keyToNodes.containsKey(attachmentKey)) {
               attachedArtifact = keyToNodes.get(attachmentKey).iterator().next().getDependency().getArtifact();
            }
            else {
               handleArtifacts(keyToNodes, nodes, attachedArtifact, referenced);
            }

            final String dataKey = attachment.isRequired() ? "requiredAttachments" : "optionalAttachments";
            for (DependencyNode node : nodes) {
               @SuppressWarnings("unchecked")
               Set<Artifact> attachedArtifacts = (Set<Artifact>) node.getData().get(dataKey);
               if (attachedArtifacts == null) {
                  attachedArtifacts = new LinkedHashSet<Artifact>();
                  node.setData(dataKey, attachedArtifacts);
               }
               attachedArtifacts.add(attachedArtifact);
            }
         }
      }
   }

   private void computeReferencedArtifacts(DependencyNode graph, final Multimap<ArtifactKey, DependencyNode> allNodes,
      final Set<ArtifactKey> referencedArtifacts) {
      graph.accept(new AbstractDependencyVisitor(false) {
         Stack<String> currentScope = new Stack<String>();

         Stack<Boolean> currentReplaced = new Stack<Boolean>();

         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node) {
            final String scope = getEffectivScope(node);

            final DependencyNode2 adapter = DependencyNode2Adapter.get(node);

            boolean replaced = adapter != null && adapter.getReplacement() != null;
            if (replaced) {
               final DependencyNode effectiveNode = getEffectiveNode(node);
               if (scopeTest || !"test".equals(scope)) {
                  final Artifact artifact = effectiveNode.getDependency().getArtifact();
                  final ArtifactKey artifactKey = toArtifactKey(artifact);
                  allNodes.put(artifactKey, effectiveNode);
                  addReferenced(allNodes, referencedArtifacts, parent, effectiveNode, artifactKey);
               }
            }

            if (!currentReplaced.isEmpty()) {
               replaced = currentReplaced.peek().booleanValue() || replaced;
            }
            currentReplaced.push(Boolean.valueOf(replaced));

            Dependency dependency = node.getDependency();
            if (dependency != null) {
               final Artifact artifact = dependency.getArtifact();
               final ArtifactKey artifactKey = toArtifactKey(artifact);
               allNodes.put(artifactKey, node);

               if (!replaced && (scopeTest || !"test".equals(scope))) {
                  addReferenced(allNodes, referencedArtifacts, parent, node, artifactKey);
               }
            }

            return super.onVisitEnter(parent, node);
         }

         private void addReferenced(final Multimap<ArtifactKey, DependencyNode> allNodes,
            final Set<ArtifactKey> referencedArtifacts, DependencyNode parent, DependencyNode node,
            ArtifactKey artifactKey) {
            boolean referenced = true;
            if (parent != null) {
               final Set<VersionConflictKey> keys1 = new DependencyNode2Adapter(parent).getConflictKeys();
               final Set<VersionConflictKey> keys2 = new DependencyNode2Adapter(node).getConflictKeys();
               if (DependencyUtils.isConflicting(keys1, keys2)) {
                  referenced = false;
               }
               else {
                  final Dependency dependency = parent.getDependency();
                  if (dependency != null) {
                     referenced = referencedArtifacts.contains(toArtifactKey(dependency.getArtifact()));
                  }
               }
            }

            if (referenced) {
               referencedArtifacts.add(artifactKey);
            }
         }


         private String getEffectivScope(DependencyNode node) {
            String scope = getNodeScope(node);
            if (!currentScope.isEmpty()) {
               scope = "test".equals(currentScope.peek()) ? "test" : scope;
            }
            currentScope.push(scope);
            return scope;
         }


         @Override
         protected boolean onVisitLeave(DependencyNode parent, DependencyNode node) {
            super.onVisitLeave(parent, node);
            currentScope.pop();
            currentReplaced.pop();
            return true;
         }

         private String getNodeScope(DependencyNode node) {
            final Dependency dependency = node.getDependency();
            return dependency == null ? "compile" : dependency.getScope();
         }
      });
   }

   private void pruneUnreferenced(DependencyNode graph) {
      final Multimap<DependencyNode, DependencyNode> foo = LinkedHashMultimap.create();

      graph.accept(new AbstractDependencyVisitor(false) {
         @Override
         protected boolean onVisitLeave(DependencyNode parent, DependencyNode node) {
            for (Iterator<DependencyNode> it = node.getChildren().iterator(); it.hasNext();) {
               final DependencyNode childNode = (DependencyNode) it.next();
               if (!isReferenced(childNode)) {
                  foo.put(node, childNode);
                  // it.remove();
               }
            }
            return super.onVisitLeave(parent, node);
         }

         private boolean isReferenced(DependencyNode node) {
            Dependency dependency = node.getDependency();
            if (dependency != null) {
               return referencedArtifacts.contains(toArtifactKey(dependency.getArtifact()));
            }
            return true;
         }
      });

      for (Entry<DependencyNode, Collection<DependencyNode>> entry : foo.asMap().entrySet()) {
         DependencyNode parent = entry.getKey();
         parent.getChildren().removeAll(entry.getValue());
      }
   }

   private Stack<ArtifactKey> treeStack = new Stack<ArtifactKey>();

   private void traverseTrees(List<DependencyNode> nodes, DependencyGraphTransformationContext context)
      throws RepositoryException {
      for (DependencyNode node : nodes) {
         final ArtifactKey artifactKey = toArtifactKey(node.getDependency().getArtifact());

         if (treeStack.contains(artifactKey)) // cycle
         {
            return;
         }

         treeStack.push(artifactKey);

         traverseTrees(node.getChildren(), context);
         traverceTree(node, node.getDependency().getArtifact(), computeTreePerArtifact, context);

         treeStack.pop();
      }
   }

   private Set<ArtifactKey> visited = new HashSet<ArtifactKey>();

   private void traverceTree(DependencyNode rootNode, Artifact rootArtifact, boolean computeTreePerArtifact,
      DependencyGraphTransformationContext context) throws RepositoryException {
      final ArtifactKey artifactKey = toArtifactKey(rootArtifact);
      if (visited.add(artifactKey)) {
         // if same node exists with different scope then test use it, otherwise we'll lose dependencies through
         // ScopeChildDependenciesErasure
         if (!scopeTest && "test".equals(rootNode.getDependency().getScope())) {
            for (DependencyNode dependencyNode : keyToNodes.get(artifactKey)) {
               if (!"test".equals(dependencyNode.getDependency().getScope())) {
                  rootNode = dependencyNode;
                  rootArtifact = dependencyNode.getDependency().getArtifact();
                  break;
               }
            }
         }

         if (computeTreePerArtifact) {
            rootNode = transformer.transformGraph(rootNode, context);
         }
         else {
            new ResetVisibility().transformGraph(rootNode, context);
            new HideDuplicatedSiblings().transformGraph(rootNode, context);
            new ApplyScopeAndOptional().transformGraph(rootNode, context);
         }

         new DependencyModelBuildingNodeTraverser(handler, new NearestDependencyNodeChooser(false), scopeTest).traverse(rootNode);
      }
   }

   private static DependencyNode getEffectiveNode(DependencyNode node) {
      final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
      final DependencyNode replacement = adapter == null ? null : adapter.getReplacement();
      if (replacement == null) {
         return node;
      }
      return getEffectiveNode(replacement);
   }
}
