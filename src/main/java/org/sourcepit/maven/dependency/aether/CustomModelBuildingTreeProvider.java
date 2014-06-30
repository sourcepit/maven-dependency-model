/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.aether;

import java.util.List;

import org.sourcepit.maven.dependency.CustomModelAdapter;
import org.sourcepit.maven.dependency.DependencyNodeRequest;
import org.sourcepit.maven.dependency.TreeProvider;

public class CustomModelBuildingTreeProvider<Model> implements TreeProvider<DependencyNodeRequest>
{
   private final TreeProvider<DependencyNodeRequest> target;

   private final CustomModelAdapter<Model> modelAdapter;

   public CustomModelBuildingTreeProvider(TreeProvider<DependencyNodeRequest> target,
      CustomModelAdapter<Model> modelAdapter)
   {
      this.target = target;
      this.modelAdapter = modelAdapter;
   }

   @Override
   public List<DependencyNodeRequest> getRoots(List<DependencyNodeRequest> requests)
   {
      final List<DependencyNodeRequest> children = target.getRoots(requests);
      adapt(children);
      return children;
   }

   @Override
   public List<DependencyNodeRequest> getChildren(DependencyNodeRequest request)
   {
      final List<DependencyNodeRequest> children = target.getChildren(request);
      adapt(children);
      return children;
   }

   private void adapt(List<DependencyNodeRequest> requests)
   {
      for (DependencyNodeRequest request : requests)
      {
         modelAdapter.adapt(request.getNode());
      }
   }
}
