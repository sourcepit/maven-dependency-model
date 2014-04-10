/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;

public class DefaultDependencyGraphSkeletonBuilder implements DependencyGraphSkeletonBuilder
{

   @Override
   public void buildGraphSkeleton(RepositorySystemSession session, GraphRequest request)
   {
      final CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRootArtifact(request.getRootArtifact());
      collectRequest.setDependencies(request.getDependencies());
      collectRequest.setManagedDependencies(request.getManagedDependencies());
      collectRequest.setRepositories(request.getRepositories());

      DependencyResolutionScope dependencyResolutionScope = request.getDependencyResolutionScope();
   }

   @Override
   public void buildGraphsSkeleton(RepositorySystemSession session, GraphsRequest request)
   {

   }
}
