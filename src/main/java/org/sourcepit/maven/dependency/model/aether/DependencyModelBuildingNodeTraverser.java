/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.common.utils.props.PropertiesMap;

public class DependencyModelBuildingNodeTraverser extends DependencyNodeTraverser
{
   private final Map<DependencyNode, String> scopeMask = new HashMap<DependencyNode, String>();

   private Set<DependencyNode> selectedNodes = new HashSet<DependencyNode>();

   private final DependencyModelHandler handler;
   private final NearestDependencyNodeChooser nodeChooser;
   private final boolean scopeTest;

   public DependencyModelBuildingNodeTraverser(DependencyModelHandler handler,
      NearestDependencyNodeChooser nodeChooser, boolean scopeTest)
   {
      this.handler = handler;
      this.nodeChooser = nodeChooser;
      this.scopeTest = scopeTest;
   }

   protected DependencyNode getEffectiveNode(DependencyNode parent, DependencyNode node)
   {
      if (parent == null)
      {
         return node;
      }
      return super.getEffectiveNode(node);
   }

   @Override
   protected boolean visitEnter(Visit visit, PropertiesMap parentProps)
   {
      if (visit.isRoot())
      {
         return visitEnterRoot(visit);
      }
      else
      {
         visitEnterNode(visit, parentProps);
      }
      return super.visitEnter(visit, parentProps);
   }

   private boolean visitEnterRoot(Visit visit)
   {
      final DependencyNode rootNode = visit.getNode();
      final boolean traverse = handler.startDependencyTree(rootNode.getDependency().getArtifact());
      if (traverse)
      {
         final DependencyNode2 adapter = DependencyNode2Adapter.get(rootNode);
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
      return traverse;
   }

   private void visitEnterNode(Visit visit, PropertiesMap parentProps)
   {
      final DependencyNode node = visit.getNode();

      final boolean optional = isOptional(node, parentProps);

      String scope = getScope(node);

      String parentScope = parentProps.get("scope");
      if (parentScope != null)
      {
         scope = DependencyUtils.getEffectiveScope(parentScope, scope, false);
      }
      parentProps.put("scope", scope);

      final DependencyNode effectiveNode = visit.getEffectiveNode();
      final DependencyNode cycleNode = visit.getCycleNode();
      final boolean cycleWithRoot = visit.getRootNode().equals(cycleNode);

      boolean selected = parentProps.getBoolean("isSelected", true);
      if (selected)
      {
         selected = isSelected(visit, scope) && selectedNodes.add(effectiveNode);
      }
      parentProps.setBoolean("isSelected", selected);

      handler.startDependencyNode(effectiveNode, scope, optional, selected, visit.isReplaced() ? node : null,
         cycleNode, cycleWithRoot);
   }

   @Override
   protected boolean visitLeave(Visit visit, PropertiesMap parentProps)
   {
      if (visit.isRoot())
      {
         visitLeaveRoot(visit);
      }
      else
      {
         handler.endDependencyNode(visit.getNode());
      }
      return super.visitLeave(visit, parentProps);
   }

   private void visitLeaveRoot(Visit visit)
   {
      scopeMask.clear();
      handler.endDependencyTree(visit.getNode().getDependency().getArtifact());
   }

   private boolean isSelected(Visit visit, String effectiveScope)
   {
      DependencyNode node = visit.getNode();
      DependencyNode effectiveNode = visit.getEffectiveNode();

      boolean selected = true;
      if (visit.getPath().size() > 1)
      {
         // if (!DependencyNode2Adapter.get(node).isVisible())
         // {
         // selected = false;
         // }
         // else
         if (node.getDependency().isOptional())
         {
            selected = false;
         }
         else if (node.getDependency().getScope().equals(effectiveScope))
         {
            if (effectiveScope.equals("test") || effectiveScope.equals("provided"))
            {
               selected = false;
            }
         }
      }

      if (selected && !scopeTest && "test".equals(effectiveScope))
      {
         selected = false;
      }

      DependencyNode currentRootNode = visit.getRootNode();

      if (selected && effectiveNode != node && currentRootNode != effectiveNode)
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
            selected = false;

            // our node is not a chosen one, but if no one else holds the ref to the effective node then we
            // have to!
            // but only if the effective node can't be located in our tree...
            if (!DependencyUtils.isParentNodeOf(currentRootNode, effectiveNode))
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
                  selected = true;
               }
            }
         }
      }

      return selected;
   }

   private String getScope(final DependencyNode node)
   {
      String scope = scopeMask.get(node);
      if (scope == null)
      {
         scope = node.getDependency().getScope();
      }
      return scope;
   }

   private boolean isOptional(DependencyNode node, PropertiesMap parentProps)
   {
      final boolean optional = parentProps.getBoolean("isOptional", node.getDependency().isOptional());
      if (optional) // inherit "true"
      {
         parentProps.setBoolean("isOptional", true);
      }
      return optional;
   }
}