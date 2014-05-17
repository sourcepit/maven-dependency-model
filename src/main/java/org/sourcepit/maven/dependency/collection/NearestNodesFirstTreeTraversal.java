/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class NearestNodesFirstTreeTraversal<Node> implements TreeTraversal<Node>
{
   @Override
   public void traverse(TreeProvider<Node> tree, Node node)
   {
      traverse(tree, tree.getChildren(node));
   }

   @Override
   public void traverse(TreeProvider<Node> tree, Collection<Node> nodes)
   {
      if (nodes != null && !nodes.isEmpty())
      {
         final List<Collection<Node>> childrens = new ArrayList<Collection<Node>>(nodes.size());
         for (Node node : nodes)
         {
            childrens.add(tree.getChildren(node));
         }
         for (Collection<Node> children : childrens)
         {
            traverse(tree, children);
         }
      }
   }
}