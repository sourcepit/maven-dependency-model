/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.impl;

import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyTree;

public final class DependencyModelOperations
{
   private DependencyModelOperations()
   {
      super();
   }

   public static DependencyTree getDependencyTree(DependencyModel model, ArtifactKey artifactKey)
   {
      for (DependencyTree tree : model.getDependencyTrees())
      {
         if (artifactKey.equals(tree.getArtifact().getArtifactKey()))
         {
            return tree;
         }
      }
      return null;
   }
}
