/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import java.util.Stack;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.common.maven.core.MavenCoreUtils;
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
            ArtifactKey artifactKey = dependency == null ? null : MavenCoreUtils.toArtifactKey(dependency
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
