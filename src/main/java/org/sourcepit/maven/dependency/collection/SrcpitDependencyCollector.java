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
      final CollectResult result;

      if (isRootRequest(request))
      {
         result = collectDependencies(session, request.getRoot(), request);
      }
      else
      {
         result = collectDependencies(session, request.getRootArtifact(), request.getDependencies(), request);
      }

      throwOnErrors(result);
      transformGraph(session, result);
      throwOnErrors(result);

      return result;
   }

   private CollectResult collectDependencies(RepositorySystemSession session, Dependency dependency,
      CollectRequest request)
   {
      final CollectResult result = new CollectResult(request);
      final TreeProvider<DependencyNodeRequest> treeProvider = newTreeProvider(result);

      final DependencyNodeContext rootContext = newRootContext(session, request);
      final DependencyNodeRequest nodeRequest = new DependencyNodeRequest();
      nodeRequest.setContext(rootContext);
      nodeRequest.setDependency(request.getRoot());

      newTreeTraversal().traverse(treeProvider, nodeRequest);
      result.setRoot(nodeRequest.getDependencyNode());

      return result;
   }

   private CollectResult collectDependencies(RepositorySystemSession session, final Artifact rootArtifact,
      List<Dependency> dependencies, CollectRequest request)
   {
      final TreeTraversal<DependencyNodeRequest> treeTraversal = newTreeTraversal();

      final CollectResult result = new CollectResult(request);
      final TreeProvider<DependencyNodeRequest> treeProvider = newTreeProvider(result);

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

      if (!dependencies.isEmpty())
      {
         final DependencyNodeContext rootContext = newRootContext(session, request);
         final DependencyNodeContext childContext = rootContext.deriveChildContext(node,
            Collections.<Dependency> emptyList(), Collections.<RemoteRepository> emptyList());

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
      }

      result.setRoot(node);

      return result;
   }

   private void transformGraph(RepositorySystemSession session, final CollectResult result)
   {
      final DependencyGraphTransformer transformer = session.getDependencyGraphTransformer();
      try
      {
         result.setRoot(transformer.transformGraph(result.getRoot(), new DefaultDependencyGraphTransformationContext(
            session)));
      }
      catch (RepositoryException e)
      {
         result.addException(e);
      }
   }

   private static void throwOnErrors(final CollectResult result) throws DependencyCollectionException
   {
      if (!result.getExceptions().isEmpty())
      {
         throw new DependencyCollectionException(result);
      }
   }

   private boolean isRootRequest(final CollectRequest request)
   {
      return request.getRoot() != null;
   }

   private TreeTraversal<DependencyNodeRequest> newTreeTraversal()
   {
      return new NearestNodesFirstTreeTraversal<DependencyNodeRequest>();
   }

   private TreeProvider<DependencyNodeRequest> newTreeProvider(final CollectResult result)
   {
      DependencyTreeProvider resolver = new DependencyTreeProvider(descriptorReader, versionRangeResolver)
      {
         @Override
         protected void addException(DependencyNodeImpl node, Exception e)
         {
            super.addException(node, e);
            result.addException(e);
         }
      };
      return new ConflictResolver(resolver);
      // return resolver;
   }

   private DependencyNodeContext newRootContext(final RepositorySystemSession session, final CollectRequest request)
   {
      final boolean savePremanagedState = ConfigUtils.getBoolean(session, false,
         DependencyManagerUtils.CONFIG_PROP_VERBOSE);

      final DependencyNodeContext context = new DependencyNodeContext(session, remoteRepositoryManager)
      {
         @Override
         public DependencyNodeContext deriveChildContext(DependencyNode parentNode,
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
