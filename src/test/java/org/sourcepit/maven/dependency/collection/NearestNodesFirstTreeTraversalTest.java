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

      public String getName()
      {
         return name;
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
      
      assertEquals(">a,>b,c,>d,e,f,g,<<<", result);
   }

   private String travers(Node a)
   {
      final StringBuilder sb = new StringBuilder();

      new NearestNodesFirstTreeTraversal<Node>().traverse(new TreeProvider<Node>()
      {
         @Override
         public List<Node> getChildren(Node parent)
         {
            sb.append(parent.getName());
            sb.append(',');
            return parent.getChildren();
         }

         @Override
         public List<Node> enter(List<Node> nodes)
         {
            sb.append('>');
            
            return nodes;
         }

         @Override
         public void leave(List<Node> nodes)
         {
            sb.append('<');
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
      
      System.out.println(result);
      
      assertEquals(">root,>x0y0,x1y0,x2y0,>x0y1,x1y1,x2y1,>x0y2,x1y2,x2y2,<<<<", result);
   }

}
