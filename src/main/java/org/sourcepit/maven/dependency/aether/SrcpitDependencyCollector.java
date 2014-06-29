/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.aether;

import static org.sourcepit.maven.dependency.aether.AdditionalDependenciesFilter.mergeDeps;
import static org.sourcepit.maven.dependency.aether.AetherDependencyNodeBuildingTreeProvider.getDependencyNode;
import static org.sourcepit.maven.dependency.aether.AetherDependencyNodeBuildingTreeProvider.setDependencyNode;

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
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.util.ConfigUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.version.Version;
import org.sourcepit.common.maven.model.VersionConflictKey;
import org.sourcepit.maven.dependency.ConflictKeyAdapter;
import org.sourcepit.maven.dependency.DependenciesFilter;
import org.sourcepit.maven.dependency.DependencyNode;
import org.sourcepit.maven.dependency.DependencyNodeManager;
import org.sourcepit.maven.dependency.DependencyNodeRequest;
import org.sourcepit.maven.dependency.TreeProvider;
import org.sourcepit.maven.dependency.TreeTraversal;
import org.sourcepit.maven.dependency.VersionChooser;
import org.sourcepit.maven.dependency.impl.ConflictSolvingTreeProvider;
import org.sourcepit.maven.dependency.impl.CycleSolvingTreeProvider;
import org.sourcepit.maven.dependency.impl.DependencyResolver;
import org.sourcepit.maven.dependency.impl.DependencyResolvingTreeProvider;
import org.sourcepit.maven.dependency.impl.HighestVersionChooser;
import org.sourcepit.maven.dependency.impl.NearestNodesFirstTreeTraversal;
import org.sourcepit.maven.dependency.impl.VersionConflictKeyAdapter;

@Named("srcpit")
public class SrcpitDependencyCollector implements DependencyCollector
{
   private final class RootNodeImpl extends DependencyNodeImpl
   {
      private final Artifact rootArtifact;

      private RootNodeImpl(Artifact rootArtifact)
      {
         this.rootArtifact = rootArtifact;
      }

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
   }

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
      final List<Dependency> managedDependencies = request.getManagedDependencies();
      final List<Dependency> dependencies = request.getDependencies();
      final List<RemoteRepository> repositories = request.getRepositories();
      final String requestContext = request.getRequestContext();

      final DependencyNodeManager nodeManager = newRootManager(session, managedDependencies, dependencies);
      final DependencyNode node = new DependencyNode(repositories, dependency);
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

      final DependencyNodeImpl node = new RootNodeImpl(rootArtifact);

      if (!dependencies.isEmpty())
      {
         final RequestTrace trace = RequestTrace.newChild(request.getTrace(), request);
         final String requestContext = request.getRequestContext();
         final List<RemoteRepository> repositories = request.getRepositories();
         final List<Dependency> managedDependencies = request.getManagedDependencies();

         final DependencyNodeManager childManager = newChildManager(session, managedDependencies);

         final DependencyNode parentNode = new DependencyNode(repositories, null);
         parentNode.setVersionToArtifactDescriptorResultMap(new HashMap<Version, ArtifactDescriptorResult>());
         setDependencyNode(parentNode, node);

         final List<DependencyNodeRequest> requests = new ArrayList<DependencyNodeRequest>(dependencies.size());
         for (Dependency dependency : dependencies)
         {
            if (childManager.selectDependency(dependency))
            {
               final DependencyNode childNode = new DependencyNode(parentNode, repositories, dependency);
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
         result.setRoot(transformer.transformGraph(result.getRoot(), new DependencyGraphTransformationContextImpl(
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

   private DependencyNodeManager newRootManager(final RepositorySystemSession session,
      final List<Dependency> managedDependencies, final List<Dependency> dependencies)
   {
      final DependencySelector dependencySelector = new DependencySelector()
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
      };
      final DependencyManager dependencyManager = new DependencyManager()
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
      };
      final DependencyTraverser dependencyTraverser = session.getDependencyTraverser();
      final DependenciesFilter dependenciesFilter = new AdditionalDependenciesFilter(dependencies);
      final DependencyNodeManager nodeManager = new DependencyNodeManager(dependencySelector, dependencyManager,
         dependencyTraverser, dependenciesFilter)
      {
         @Override
         public DependencyNodeManager deriveChildManager(RepositorySystemSession session, Dependency parent,
            List<Dependency> managedDeps)
         {
            return super.deriveChildManager(session, parent, mergeDeps(managedDependencies, managedDeps));
         }
      };
      return nodeManager;
   }

   private DependencyNodeManager newChildManager(RepositorySystemSession session,
      final List<Dependency> managedDependencies)
   {
      final DependencyNodeManager rootManager = newRootManager(session, managedDependencies,
         Collections.<Dependency> emptyList());
      return rootManager.deriveChildManager(session, null, Collections.<Dependency> emptyList());
   }
}
