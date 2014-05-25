/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class NearestNodesFirstTreeTraversalTest
{
   private static class Node
   {
      private String name;

      private List<Node> children = new ArrayList<Node>();

      public Node(String name)
      {
         this.name = name;
      }

      public List<Node> getChildren()
      {
         return children;
      }

      @Override
      public String toString()
      {
         return name;
      }
   }

   @Test
   public void test()
   {
      Node a = new Node("a");
      Node b = new Node("b");
      Node c = new Node("c");
      Node d = new Node("d");
      Node e = new Node("e");
      Node f = new Node("f");
      Node g = new Node("g");

      a.getChildren().add(b);
      a.getChildren().add(c);

      b.getChildren().add(d);
      b.getChildren().add(e);

      c.getChildren().add(f);
      c.getChildren().add(g);

      String result = travers(a);

      StringBuilder sb = new StringBuilder();
      sb.append("visitChildren(null, 0, [a])\n");
      sb.append("getChildren(a)\n");
      sb.append("visitChildren(a, 1, [b, c])\n");
      sb.append("getChildren(b)\n");
      sb.append("getChildren(c)\n");
      sb.append("visitChildren(b, 2, [d, e])\n");
      sb.append("getChildren(d)\n");
      sb.append("getChildren(e)\n");
      sb.append("visitChildren(c, 2, [f, g])\n");
      sb.append("getChildren(f)\n");
      sb.append("getChildren(g)\n");
      sb.append("leaveChildren(c, 2, [f, g])\n");
      sb.append("leaveChildren(b, 2, [d, e])\n");
      sb.append("leaveChildren(a, 1, [b, c])\n");
      sb.append("leaveChildren(null, 0, [a])\n");

      assertEquals(sb.toString(), result);
   }

   private String travers(Node a)
   {
      final StringBuilder sb = new StringBuilder();

      new NearestNodesFirstTreeTraversal<Node>().traverse(new TreeProvider<Node>()
      {
         @Override
         public List<Node> visitChildren(Node parent, int depth, List<Node> children)
         {
            enterOrLeave("visitChildren", parent, depth, children);
            return children;
         }

         @Override
         public List<Node> getChildren(Node parent)
         {
            sb.append("getChildren(");
            sb.append(parent);
            sb.append(")");
            sb.append("\n");
            return parent.getChildren();
         }

         @Override
         public void leaveChildren(Node parent, int depth, List<Node> children)
         {
            enterOrLeave("leaveChildren", parent, depth, children);
         }

         private void enterOrLeave(String method, Node parent, int depth, List<Node> children)
         {
            sb.append(method);
            sb.append("(");
            sb.append(parent == null ? "null" : parent.toString());
            sb.append(", ");
            sb.append(depth);
            sb.append(", [");
            for (Node child : children)
            {
               sb.append(child);
               sb.append(", ");
            }
            if (!children.isEmpty())
            {
               sb.deleteCharAt(sb.length() - 1);
               sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("])");
            sb.append("\n");
         }
      }, a);

      String result = sb.toString();
      return result;
   }

   @Test
   public void test2()
   {
      Node x0y0 = new Node("x0y0");
      Node x0y1 = new Node("x0y1");
      Node x0y2 = new Node("x0y2");

      x0y0.getChildren().add(x0y1);
      x0y1.getChildren().add(x0y2);

      Node x1y0 = new Node("x1y0");
      Node x1y1 = new Node("x1y1");
      Node x1y2 = new Node("x1y2");

      x1y0.getChildren().add(x1y1);
      x1y1.getChildren().add(x1y2);

      Node x2y0 = new Node("x2y0");
      Node x2y1 = new Node("x2y1");
      Node x2y2 = new Node("x2y2");

      x2y0.getChildren().add(x2y1);
      x2y1.getChildren().add(x2y2);

      Node root = new Node("root");
      root.getChildren().add(x0y0);
      root.getChildren().add(x1y0);
      root.getChildren().add(x2y0);

      String result = travers(root);
      StringBuilder sb = new StringBuilder();
      sb.append("visitChildren(null, 0, [root])\n");
      sb.append("getChildren(root)\n");
      sb.append("visitChildren(root, 1, [x0y0, x1y0, x2y0])\n");
      sb.append("getChildren(x0y0)\n");
      sb.append("getChildren(x1y0)\n");
      sb.append("getChildren(x2y0)\n");
      sb.append("visitChildren(x0y0, 2, [x0y1])\n");
      sb.append("getChildren(x0y1)\n");
      sb.append("visitChildren(x1y0, 2, [x1y1])\n");
      sb.append("getChildren(x1y1)\n");
      sb.append("visitChildren(x2y0, 2, [x2y1])\n");
      sb.append("getChildren(x2y1)\n");
      sb.append("visitChildren(x0y1, 3, [x0y2])\n");
      sb.append("getChildren(x0y2)\n");
      sb.append("visitChildren(x1y1, 3, [x1y2])\n");
      sb.append("getChildren(x1y2)\n");
      sb.append("visitChildren(x2y1, 3, [x2y2])\n");
      sb.append("getChildren(x2y2)\n");
      sb.append("leaveChildren(x2y1, 3, [x2y2])\n");
      sb.append("leaveChildren(x1y1, 3, [x1y2])\n");
      sb.append("leaveChildren(x0y1, 3, [x0y2])\n");
      sb.append("leaveChildren(x2y0, 2, [x2y1])\n");
      sb.append("leaveChildren(x1y0, 2, [x1y1])\n");
      sb.append("leaveChildren(x0y0, 2, [x0y1])\n");
      sb.append("leaveChildren(root, 1, [x0y0, x1y0, x2y0])\n");
      sb.append("leaveChildren(null, 0, [root])\n");

      assertEquals(sb.toString(), result);
   }

}
