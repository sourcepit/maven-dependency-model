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

   private static class N<Node>
   {
      public int depth;

      public List<Node> children;
   }

   @Override
   public void traverse(TreeProvider<Node> tree, List<Node> nodes)
   {
      if (nodes != null && !nodes.isEmpty())
      {
         final N<Node> n = new N<Node>();
         n.depth = 0;
         n.children = tree.getRoots(nodes);
         traverse0(tree, Collections.singletonList(n));
      }
   }

   private void traverse0(TreeProvider<Node> tree, List<N<Node>> nodes)
   {
      final List<N<Node>> next = new ArrayList<N<Node>>();
      for (N<Node> n : nodes)
      {
         for (Node child : n.children)
         {
            final List<Node> children = tree.getChildren(child);
            if (!children.isEmpty())
            {
               final N<Node> cn = new N<Node>();
               cn.depth = n.depth + 1;
               cn.children = children;
               next.add(cn);
            }
         }
      }
      if (!next.isEmpty())
      {
         traverse0(tree, next);
      }
   }

}