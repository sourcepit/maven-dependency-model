/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.collection.DependencyManagement;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.ConfigUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;

@Named("srcpit")
public class SrcpitDependencyCollector implements DependencyCollector
{
   @Inject
   private RemoteRepositoryManager remoteRepositoryManager;

   @Inject
   private ArtifactDescriptorReader descriptorReader;

   @Inject
   private VersionRangeResolver versionRangeResolver;

   @Override
   public CollectResult collectDependencies(final RepositorySystemSession session, final CollectRequest request)
      throws DependencyCollectionException
   {
      final DependencyNodeContext rootContext = newRootContext(session, request);

      final CollectResult result = new CollectResult(request);

      final TreeTraversal<DependencyNodeRequest> treeTraversal = newTreeTraversal();
      final TreeProvider<DependencyNodeRequest> treeProvider = newTreeProvider(result);

      if (isRootRequest(request))
      {
         final DependencyNodeRequest nodeRequest = new DependencyNodeRequest();
         nodeRequest.setContext(rootContext);
         nodeRequest.setDependency(request.getRoot());

         treeTraversal.traverse(treeProvider, nodeRequest);
         result.setRoot(nodeRequest.getDependencyNode());
      }
      else
      {
         final Artifact rootArtifact = request.getRootArtifact();
         final DependencyNodeImpl node = new DependencyNodeImpl()
         {
            @Override
            public void setDependency(Dependency dependency)
            {
               throw new IllegalStateException();
            }

            @Override
            public Artifact getArtifact()
            {
               return rootArtifact;
            }
         };

         final DependencyNodeContext childContext = rootContext.deriveChildContext(node,
            Collections.<Dependency> emptyList(), Collections.<RemoteRepository> emptyList());

         final List<Dependency> dependencies = request.getDependencies();
         final List<DependencyNodeRequest> requests = new ArrayList<DependencyNodeRequest>(dependencies.size());
         for (Dependency dependency : dependencies)
         {
            if (childContext.getDependencySelector().selectDependency(dependency))
            {
               final DependencyNodeRequest nodeRequest = new DependencyNodeRequest();
               nodeRequest.setContext(childContext);
               nodeRequest.setDependency(dependency);
               requests.add(nodeRequest);
            }
         }
         treeTraversal.traverse(treeProvider, requests);

         result.setRoot(node);
      }
      
      if (!result.getExceptions().isEmpty())
      {
         throw new DependencyCollectionException(result);
      }

      final DependencyNode node = result.getRoot();

      final DependencyGraphTransformer transformer = session.getDependencyGraphTransformer();
      try
      {
         result.setRoot(transformer.transformGraph(node, new DefaultDependencyGraphTransformationContext(session)));
      }
      catch (RepositoryException e)
      {
         result.addException(e);
      }

      if (!result.getExceptions().isEmpty())
      {
         throw new DependencyCollectionException(result);
      }

      return result;
   }

   private boolean isRootRequest(final CollectRequest request)
   {
      return request.getRoot() != null;
   }

   private TreeTraversal<DependencyNodeRequest> newTreeTraversal()
   {
      return new NearestNodesFirstTreeTraversal<DependencyNodeRequest>();
   }

   private DependencyTreeProvider newTreeProvider(final CollectResult result)
   {
      return new DependencyTreeProvider(descriptorReader, versionRangeResolver)
      {
         @Override
         protected void addException(DependencyNodeImpl node, Exception e)
         {
            super.addException(node, e);
            result.addException(e);
         }
      };
   }

   private DependencyNodeContext newRootContext(final RepositorySystemSession session, final CollectRequest request)
   {
      final boolean savePremanagedState = ConfigUtils.getBoolean(session, false,
         DependencyManagerUtils.CONFIG_PROP_VERBOSE);

      final DependencyNodeContext context = new DependencyNodeContext(session, remoteRepositoryManager)
      {
         @Override
         public DependencyNodeContext deriveChildContext(DependencyNodeImpl parentNode,
            List<Dependency> managedDependencies, List<RemoteRepository> repositories)
         {
            return super.deriveChildContext(parentNode,
               AdditionalDependenciesFilter.mergeDeps(request.getManagedDependencies(), managedDependencies),
               repositories);
         }
      };

      context.setRequestContext(request.getRequestContext());
      context.setRequestTrace(RequestTrace.newChild(request.getTrace(), request));
      context.setSavePremanagedState(savePremanagedState);
      context.setRepositories(request.getRepositories());
      context.setDependencySelector(new DependencySelector()
      {
         @Override
         public boolean selectDependency(Dependency dependency)
         {
            return true;
         }

         @Override
         public DependencySelector deriveChildSelector(DependencyCollectionContext context)
         {
            return session.getDependencySelector().deriveChildSelector(context);
         }
      });
      context.setDependencyManager(new DependencyManager()
      {
         @Override
         public DependencyManagement manageDependency(Dependency dependency)
         {
            return null;
         }

         @Override
         public DependencyManager deriveChildManager(DependencyCollectionContext context)
         {
            return session.getDependencyManager().deriveChildManager(context);
         }
      });
      context.setDependencyTraverser(session.getDependencyTraverser());
      context.setDependenciesFilter(new AdditionalDependenciesFilter(request.getDependencies()));
      return context;
   }
}
