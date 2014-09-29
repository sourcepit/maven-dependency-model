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
