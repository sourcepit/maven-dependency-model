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

package org.sourcepit.maven.dependency.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sourcepit.maven.dependency.TreeProvider;
import org.sourcepit.maven.dependency.TreeTraversal;

public class NearestNodesFirstTreeTraversal<Node> implements TreeTraversal<Node>
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