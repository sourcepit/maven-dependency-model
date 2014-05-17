/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
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

      final StringBuilder sb = new StringBuilder();

      new NearestNodesFirstTreeTraversal<Node>().traverse(new TreeProvider<Node>()
      {
         @Override
         public Collection<Node> getChildren(Node parent)
         {
            sb.append(parent.getName());
            return parent.getChildren();
         }
      }, a);

      assertEquals("abcdefg", sb.toString());
   }

}
