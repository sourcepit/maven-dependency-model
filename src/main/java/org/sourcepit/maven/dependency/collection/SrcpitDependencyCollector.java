/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.ConfigUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.sourcepit.maven.dependency.collection.DependencyTreeProvider.DependencyNodeContext;
import org.sourcepit.maven.dependency.collection.DependencyTreeProvider.DependencyNodeRequest;

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
      final RequestTrace trace = RequestTrace.newChild(request.getTrace(), request);

      CollectResult result = new CollectResult(request);

      final Dependency root = request.getRoot();
      if (root != null)
      {
         final DependencyNodeContext context = newRootContext(session, request.getRequestContext(), trace, request.getDependencies(),
            request.getManagedDependencies(), request.getRepositories());

         DependencyNodeRequest nodeRequest = new DependencyNodeRequest();
         nodeRequest.setContext(context);
         nodeRequest.setDependency(root);

         TreeTraversal<DependencyNodeRequest> treeTraversal = newTreeTraversal();
         TreeProvider<DependencyNodeRequest> treeProvider = newTreeProvider(session);
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

         final DependencyNodeContext context = newChildContext(session, node, request.getRequestContext(), trace,
            request.getManagedDependencies(), request.getRepositories());

         final List<Dependency> dependencies = request.getDependencies();
         final List<DependencyNodeRequest> requests = new ArrayList<DependencyNodeRequest>(dependencies.size());
         for (Dependency dependency : dependencies)
         {
            if (context.getDependencySelector().selectDependency(dependency))
            {
               DependencyNodeRequest nodeRequest = new DependencyNodeRequest();
               nodeRequest.setContext(context);
               nodeRequest.setDependency(dependency);
               requests.add(nodeRequest);
            }
         }

         TreeTraversal<DependencyNodeRequest> treeTraversal = newTreeTraversal();
         TreeProvider<DependencyNodeRequest> treeProvider = newTreeProvider(session);

         treeTraversal.traverse(treeProvider, requests);

         result.setRoot(node);
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

      return result;
   }

   private TreeTraversal<DependencyNodeRequest> newTreeTraversal()
   {
      return new NearestNodesFirstTreeTraversal<DependencyNodeRequest>();
   }

   private DependencyTreeProvider newTreeProvider(RepositorySystemSession session)
   {
      return new DependencyTreeProvider(remoteRepositoryManager, descriptorReader, versionRangeResolver, session);
   }

   private static DependencyNodeContext newRootContext(final RepositorySystemSession session, String requestContext,
      RequestTrace requestTrace, final List<Dependency> dependencies, final List<Dependency> managedDependencies,
      List<RemoteRepository> repositories)
   {
      final boolean savePremanagedState = ConfigUtils.getBoolean(session, false,
         DependencyManagerUtils.CONFIG_PROP_VERBOSE);

      final DependencyNodeContext context = new DependencyNodeContext()
      {
         @Override
         public DependencyNodeContext deriveChildContext(RemoteRepositoryManager remoteRepositoryManager,
            RepositorySystemSession session, DependencyNodeImpl parentNode, List<Dependency> managedDepes,
            List<RemoteRepository> repositories)
         {
            return super.deriveChildContext(remoteRepositoryManager, session, parentNode,
               mergeDeps(managedDependencies, managedDepes), repositories);
         }
      };

      context.setRequestContext(requestContext);
      context.setRequestTrace(requestTrace);
      context.setSavePremanagedState(savePremanagedState);
      context.setRepositories(repositories);
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

      context.setDependenciesFilter(new DependenciesFilter()
      {
         @Override
         public List<Dependency> filterDependencies(Artifact artifact, List<Dependency> deps)
         {
            return mergeDeps(dependencies, deps);
         }

         @Override
         public DependenciesFilter deriveChildFilter(DependencyCollectionContext context)
         {
            return new NoopDependenciesFilter();
         }
      });

      return context;
   }

   private static List<Dependency> mergeDeps(List<Dependency> dominant, List<Dependency> recessive)
   {
      List<Dependency> result;
      if (dominant == null || dominant.isEmpty())
      {
         result = recessive;
      }
      else if (recessive == null || recessive.isEmpty())
      {
         result = dominant;
      }
      else
      {
         result = new ArrayList<Dependency>(dominant.size() + recessive.size());
         Collection<String> ids = new HashSet<String>();
         for (Dependency dependency : dominant)
         {
            ids.add(getId(dependency.getArtifact()));
            result.add(dependency);
         }
         for (Dependency dependency : recessive)
         {
            if (!ids.contains(getId(dependency.getArtifact())))
            {
               result.add(dependency);
            }
         }
      }
      return result;
   }

   private static String getId(Artifact a)
   {
      return a.getGroupId() + ':' + a.getArtifactId() + ':' + a.getClassifier() + ':' + a.getExtension();
   }

   private static DependencyNodeContext newChildContext(RepositorySystemSession session, DependencyNodeImpl node,
      String requestContext, RequestTrace requestTrace, List<Dependency> managedDependencies,
      List<RemoteRepository> repositories)
   {
      final boolean savePremanagedState = ConfigUtils.getBoolean(session, false,
         DependencyManagerUtils.CONFIG_PROP_VERBOSE);

      final DefaultDependencyCollectionContext collectionContext = new DefaultDependencyCollectionContext(session,
         node.getDependency(), managedDependencies);

      final DependencyNodeContext context = new DependencyNodeContext();
      context.setRequestTrace(requestTrace);
      context.getParentNodes().add(node);
      context.setSavePremanagedState(savePremanagedState);
      context.setRequestContext(requestContext);
      context.setRepositories(repositories);
      context.setDependencySelector(session.getDependencySelector().deriveChildSelector(collectionContext));
      context.setDependencyManager(session.getDependencyManager().deriveChildManager(collectionContext));
      context.setDependencyTraverser(session.getDependencyTraverser().deriveChildTraverser(collectionContext));
      context.setDependenciesFilter(new NoopDependenciesFilter());
      return context;
   }

}
