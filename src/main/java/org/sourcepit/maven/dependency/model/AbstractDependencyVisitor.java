/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import java.util.Stack;

import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

public abstract class AbstractDependencyVisitor implements DependencyVisitor
{
   protected Stack<DependencyNode> path = new Stack<DependencyNode>();

   @Override
   public final boolean visitEnter(DependencyNode node)
   {
      final boolean cycle = path.contains(node);
      if (cycle)
      {
         path.push(node);
         return false;
      }
      else
      {
         final boolean result = onVisitEnter(getParent(), node);
         path.push(node);
         return result;
      }
   }

   private DependencyNode getParent()
   {
      return path.isEmpty() ? null : path.peek();
   }

   protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
   {
      return true;
   }

   @Override
   public final boolean visitLeave(DependencyNode node)
   {
      path.pop();
      return onVisitLeave(getParent(), node);
   }

   protected boolean onVisitLeave(DependencyNode parent, DependencyNode node)
   {
      return true;
   }
}
