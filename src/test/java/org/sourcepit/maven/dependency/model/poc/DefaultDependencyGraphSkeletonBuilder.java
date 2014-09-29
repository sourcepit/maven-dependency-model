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
