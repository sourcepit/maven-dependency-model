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

import java.util.Stack;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.common.maven.artifact.MavenArtifactUtils;
import org.sourcepit.common.maven.model.ArtifactKey;

public class ApplyScopeAndOptional implements DependencyGraphTransformer
{
   @Override
   public DependencyNode transformGraph(DependencyNode graph, DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      graph.accept(new AbstractDependencyVisitor(false)
      {
         Stack<ArtifactKey> path = new Stack<ArtifactKey>();

         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            Dependency dependency = node.getDependency();
            ArtifactKey artifactKey = dependency == null ? null : MavenArtifactUtils.toArtifactKey(dependency
               .getArtifact());

            if (path.contains(artifactKey))
            {
               path.push(artifactKey);
               return false;
            }

            final int depth = path.size();
            boolean visible = DependencyNode2Adapter.get(node).isVisible();
            if (visible && depth > 1)
            {
               DependencyNode2 parentAdapter = DependencyNode2Adapter.get(parent);
               visible = parentAdapter.isVisible();
               if (visible)
               {
                  final String scope = dependency.getScope();
                  if (dependency.isOptional() || scope.equals("test") || scope.equals("provided"))
                  {
                     visible = false;
                  }
               }
            }
            DependencyNode2Adapter.get(node).setVisible(visible);
            path.push(artifactKey);
            return true;
         }

         @Override
         protected boolean onVisitLeave(DependencyNode parent, DependencyNode node)
         {
            path.pop();
            return super.onVisitLeave(parent, node);
         }
      });
      return graph;
   }
}
