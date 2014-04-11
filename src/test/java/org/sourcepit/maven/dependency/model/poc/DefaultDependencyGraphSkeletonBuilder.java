/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.util.graph.transformer.NoopDependencyGraphTransformer;

@Named
public class DefaultDependencyGraphSkeletonBuilder implements DependencyGraphSkeletonBuilder
{
   private final DependencyCollector dependencyCollector;

   @Inject
   public DefaultDependencyGraphSkeletonBuilder(DependencyCollector dependencyCollector)
   {
      this.dependencyCollector = dependencyCollector;
   }

   @Override
   public CollectResult buildGraphSkeleton(final RepositorySystemSession session, GraphRequest request)
      throws DependencyCollectionException
   {
      final CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRootArtifact(request.getRootArtifact());
      collectRequest.setDependencies(request.getDependencies());
      collectRequest.setManagedDependencies(request.getManagedDependencies());
      collectRequest.setRepositories(request.getRepositories());

      return dependencyCollector.collectDependencies(new AbstractForwardingRepositorySystemSession()
      {
         @Override
         protected RepositorySystemSession getSession()
         {
            return session;
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer()
         {
            return new NoopDependencyGraphTransformer();
         }

      }, collectRequest);
   }

   @Override
   public CollectResult buildGraphsSkeleton(final RepositorySystemSession session, GraphsRequest request)
      throws DependencyCollectionException
   {
      final CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRootArtifact(request.getRootArtifact());
      collectRequest.setDependencies(request.getDependencies());
      collectRequest.setManagedDependencies(request.getManagedDependencies());
      collectRequest.setRepositories(request.getRepositories());

      return dependencyCollector.collectDependencies(new AbstractForwardingRepositorySystemSession()
      {
         @Override
         protected RepositorySystemSession getSession()
         {
            return session;
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer()
         {
            return new NoopDependencyGraphTransformer();
         }

      }, collectRequest);
   }
}
