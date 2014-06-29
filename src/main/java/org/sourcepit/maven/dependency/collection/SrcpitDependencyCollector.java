/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import static org.sourcepit.maven.dependency.collection.AetherDependencyNodeBuildingTreeProvider.getDependencyNode;
import static org.sourcepit.maven.dependency.collection.AetherDependencyNodeBuildingTreeProvider.setDependencyNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.util.ConfigUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.version.Version;
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
      final boolean savePremanagedState = ConfigUtils.getBoolean(session, false,
         DependencyManagerUtils.CONFIG_PROP_VERBOSE);

      final CollectResult result = new CollectResult(request);

      final DependencyNodeManager nodeManager = newRootContext(session, request);
      final String requestContext = request.getRequestContext();
      final DependencyResolutionNode node = new DependencyResolutionNode(request.getRepositories(), dependency);
      final RequestTrace trace = RequestTrace.newChild(request.getTrace(), request);
      final DependencyNodeRequest nodeRequest = new DependencyNodeRequest(session, trace, requestContext, nodeManager,
         node);

      final TreeProvider<DependencyNodeRequest> treeProvider = newTreeProvider(result, savePremanagedState);
      newTreeTraversal().traverse(treeProvider, nodeRequest);
      result.setRoot(getDependencyNode(node));

      return result;
   }

   private CollectResult collectDependencies(RepositorySystemSession session, final Artifact rootArtifact,
      List<Dependency> dependencies, CollectRequest request)
   {
      final TreeTraversal<DependencyNodeRequest> treeTraversal = newTreeTraversal();

      final boolean savePremanagedState = ConfigUtils.getBoolean(session, false,
         DependencyManagerUtils.CONFIG_PROP_VERBOSE);

      final CollectResult result = new CollectResult(request);
      final TreeProvider<DependencyNodeRequest> treeProvider = newTreeProvider(result, savePremanagedState);

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
         final DependencyNodeManager rootManager = newRootContext(session, request);
         final DependencyNodeManager childManager = rootManager.deriveChildManager(session, null,
            Collections.<Dependency> emptyList());

         final DependencyResolutionNode parentNode = new DependencyResolutionNode(request.getRepositories(), null);
         setDependencyNode(parentNode, node);
         parentNode.setVersionToArtifactDescriptorResultMap(new HashMap<Version, ArtifactDescriptorResult>());

         final RequestTrace trace = RequestTrace.newChild(request.getTrace(), request);
         final String requestContext = request.getRequestContext();

         final List<DependencyNodeRequest> requests = new ArrayList<DependencyNodeRequest>(dependencies.size());
         for (Dependency dependency : dependencies)
         {
            if (childManager.selectDependency(dependency))
            {
               final DependencyResolutionNode childNode = new DependencyResolutionNode(parentNode,
                  request.getRepositories(), dependency);
               requests.add(new DependencyNodeRequest(session, trace, requestContext, childManager, childNode));
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

   private TreeProvider<DependencyNodeRequest> newTreeProvider(final CollectResult result, boolean savePremanagedState)
   {
      final VersionChooser versionChooser = new HighestVersionChooser();

      final TreeProvider<DependencyNodeRequest> nodeResolver = new DependencyResolvingTreeProvider(
         remoteRepositoryManager, dependencyResolver, versionChooser);

      final ConflictKeyAdapter<Set<VersionConflictKey>> conflictKeyAdapter = new VersionConflictKeyAdapter();

      final TreeProvider<DependencyNodeRequest> cycleSolver = new CycleSolvingTreeProvider<Set<VersionConflictKey>>(
         nodeResolver, conflictKeyAdapter);

      final TreeProvider<DependencyNodeRequest> conflictSolver = new ConflictSolvingTreeProvider<Set<VersionConflictKey>>(
         cycleSolver, conflictKeyAdapter);

      return new AetherDependencyNodeBuildingTreeProvider(conflictSolver, savePremanagedState)
      {
         @Override
         protected void handleException(Exception e)
         {
            super.handleException(e);
            result.addException(e);
         }
      };
   }

   private DependencyNodeManager newRootContext(final RepositorySystemSession session, final CollectRequest request)
   {
      final DependencyNodeManager context = new DependencyNodeManager()
      {
         @Override
         public DependencyNodeManager deriveChildManager(RepositorySystemSession session, Dependency parent,
            List<Dependency> managedDependencies)
         {
            return super.deriveChildManager(session, parent,
               AdditionalDependenciesFilter.mergeDeps(request.getManagedDependencies(), managedDependencies));
         }
      };

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
