/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.common.maven.model.VersionConflictKey;

public class HideDuplicatedSiblings implements DependencyGraphTransformer
{
   @Override
   public DependencyNode transformGraph(DependencyNode graph, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      graph.accept(new AbstractDependencyVisitor(false)
      {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            List<DependencyNode> children = node.getChildren();
            for (DependencyNode child : children)
            {
               foo(child, children);
            }
            return true;
         }

         private void foo(DependencyNode node, List<DependencyNode> siblings)
         {
            List<DependencyNode> conflicts = new ArrayList<DependencyNode>();

            final Set<VersionConflictKey> conflictKeys = DependencyNode2Adapter.get(node).getConflictKeys();
            for (DependencyNode sibling : siblings)
            {
               if (sibling == node)
               {
                  conflicts.add(node);
                  continue;
               }
               final Set<VersionConflictKey> conflictKeys2 = DependencyNode2Adapter.get(sibling).getConflictKeys();
               if (DependencyUtils.isConflicting(conflictKeys, conflictKeys2))
               {
                  conflicts.add(sibling);
               }
            }

            for (int i = 0; i < conflicts.size() - 1; i++)
            {
               DependencyNode2Adapter.get(conflicts.get(i)).setVisible(false);
            }
         }
      });
      return graph;
   }

}
