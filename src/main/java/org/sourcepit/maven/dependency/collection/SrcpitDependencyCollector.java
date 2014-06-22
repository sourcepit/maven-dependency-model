/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.ConfigUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.sourcepit.common.maven.model.VersionConflictKey;

@Named("srcpit")
public class SrcpitDependencyCollector implements DependencyCollector
{
   private final RemoteRepositoryManager remoteRepositoryManager;

   private final DependencyResolver dependencyResolver;

   @Inject
   public SrcpitDependencyCollector(RemoteRepositoryManager remoteRepositoryManager,
      DependencyResolver descriptorResolver)
   {
      this.remoteRepositoryManager = remoteRepositoryManager;
      this.dependencyResolver = descriptorResolver;
   }

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
      final TreeProvider<DependencyResolutionNode> treeProvider = newTreeProvider(result);

      final DependencyNodeContext rootContext = newRootContext(session, request);
      final DependencyResolutionNode nodeRequest = new DependencyResolutionNode(null);
      nodeRequest.setContext(rootContext);
      nodeRequest.setDependencyResolutionRequest(DependencyTreeProvider.newDependencyResolutionRequest(rootContext,
         request.getRoot()));

      newTreeTraversal().traverse(treeProvider, nodeRequest);
      result.setRoot(nodeRequest.getDependencyNode());

      return result;
   }

   private CollectResult collectDependencies(RepositorySystemSession session, final Artifact rootArtifact,
      List<Dependency> dependencies, CollectRequest request)
   {
      final TreeTraversal<DependencyResolutionNode> treeTraversal = newTreeTraversal();

      final CollectResult result = new CollectResult(request);
      final TreeProvider<DependencyResolutionNode> treeProvider = newTreeProvider(result);

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
         final DependencyNodeContext childContext = rootContext.deriveChildContext(null,
            Collections.<Dependency> emptyList(), Collections.<RemoteRepository> emptyList());
         
         final DependencyResolutionNode parentNode = new DependencyResolutionNode(null);
         parentNode.setContext(rootContext);
         parentNode.setConflictKeys(new HashSet<VersionConflictKey>());
         parentNode.setDependencyNode(node);

         final List<DependencyResolutionNode> requests = new ArrayList<DependencyResolutionNode>(dependencies.size());
         for (Dependency dependency : dependencies)
         {
            if (childContext.getDependencySelector().selectDependency(dependency))
            {
               final DependencyResolutionNode nodeRequest = new DependencyResolutionNode(parentNode);
               nodeRequest.setContext(childContext);
               nodeRequest.setDependencyResolutionRequest(DependencyTreeProvider.newDependencyResolutionRequest(
                  childContext, dependency));
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

   private TreeTraversal<DependencyResolutionNode> newTreeTraversal()
   {
      return new NearestNodesFirstTreeTraversal<DependencyResolutionNode>();
   }

   private TreeProvider<DependencyResolutionNode> newTreeProvider(final CollectResult result)
   {
      return new AetherDependencyNodeBuilder(new ConflictResolver(new DependencyTreeProvider(dependencyResolver)))
      {
         @Override
         protected void handleException(Exception e)
         {
            super.handleException(e);
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
         public DependencyNodeContext deriveChildContext(Dependency parent, List<Dependency> managedDependencies,
            List<RemoteRepository> repositories)
         {
            return super.deriveChildContext(parent,
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
