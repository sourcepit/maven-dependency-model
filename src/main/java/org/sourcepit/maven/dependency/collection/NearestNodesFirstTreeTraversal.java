/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class NearestNodesFirstTreeTraversal<Node> implements TreeTraversal<Node>
{
   @Override
   public void traverse(TreeProvider<Node> tree, Node node)
   {
      traverse(tree, Collections.singletonList(node));
   }

   @Override
   public void traverse(TreeProvider<Node> tree, List<Node> nodes)
   {
      if (nodes != null && !nodes.isEmpty())
      {
         final List<Node> childrens = new ArrayList<Node>();
         for (Node node : tree.enter(nodes))
         {
            childrens.addAll(tree.getChildren(node));
         }
         traverse(tree, childrens);
         tree.leave(nodes);
      }
   }

}