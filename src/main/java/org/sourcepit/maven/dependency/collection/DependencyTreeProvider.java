/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.Collection;

import javax.inject.Inject;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.VersionRangeResolver;

public class DependencyTreeProvider implements TreeProvider<DependencyNode>
{
   @Inject
   private RemoteRepositoryManager remoteRepositoryManager;

   @Inject
   private ArtifactDescriptorReader descriptorReader;

   @Inject
   private VersionRangeResolver versionRangeResolver;

   public DependencyTreeProvider(RepositorySystemSession session, CollectRequest request)
   {
   }

   @Override
   public Collection<DependencyNode> getChildren(DependencyNode parent)
   {
      // if selectDependency
      // apply dependency management
      // resolve version range
      // read artifact descriptor
      // handle cycle
      // handle relocation
      // traverse children (if traverse dependencies)
      return null;
   }

}
