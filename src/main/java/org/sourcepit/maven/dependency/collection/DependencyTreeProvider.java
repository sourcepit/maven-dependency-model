/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyManagement;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;

public class DependencyTreeProvider implements TreeProvider<DependencyTreeProvider.Request>
{
   @Inject
   private RemoteRepositoryManager remoteRepositoryManager;

   @Inject
   private ArtifactDescriptorReader descriptorReader;

   @Inject
   private VersionRangeResolver versionRangeResolver;

   private final RepositorySystemSession session;

   public DependencyTreeProvider(RepositorySystemSession session)
   {
      this.session = session;
   }

   public static class Request
   {
      private Context context;

      private Dependency dependency;

      public void setContext(Context context)
      {
         this.context = context;
      }

      public Context getContext()
      {
         return context;
      }

      public void setDependency(Dependency dependency)
      {
         this.dependency = dependency;
      }

      public Dependency getDependency()
      {
         return dependency;
      }
   }

   public static class Context
   {
      private DependencySelector dependencySelector;

      private DependencyManager dependencyManagemer;

      private DependencyTraverser dependencyTraverser;

      private boolean savePremanagedState;

      private String requestContext;

      private RequestTrace requestTrace;

      private List<RemoteRepository> repositories;

      public void setDependencySelector(DependencySelector dependencySelector)
      {
         this.dependencySelector = dependencySelector;
      }

      public DependencySelector getDependencySelector()
      {
         return dependencySelector;
      }

      public void setDependencyManagemer(DependencyManager dependencyManagemer)
      {
         this.dependencyManagemer = dependencyManagemer;
      }

      public DependencyManager getDependencyManagemer()
      {
         return dependencyManagemer;
      }

      public void setDependencyTraverser(DependencyTraverser dependencyTraverser)
      {
         this.dependencyTraverser = dependencyTraverser;
      }

      public DependencyTraverser getDependencyTraverser()
      {
         return dependencyTraverser;
      }

      public void setSavePremanagedState(boolean savePremanagedState)
      {
         this.savePremanagedState = savePremanagedState;
      }

      public boolean isSavePremanagedState()
      {
         return savePremanagedState;
      }

      public void setRequestContext(String requestContext)
      {
         this.requestContext = requestContext;
      }

      public String getRequestContext()
      {
         return requestContext;
      }

      public RequestTrace getRequestTrace()
      {
         return requestTrace;
      }

      public void setRequestTrace(RequestTrace requestTrace)
      {
         this.requestTrace = requestTrace;
      }

      public List<RemoteRepository> getRepositories()
      {
         if (repositories == null)
         {
            repositories = new ArrayList<RemoteRepository>(0);
         }
         return repositories;
      }

      public void setRepositories(List<RemoteRepository> repositories)
      {
         this.repositories = repositories;
      }
   }

   @Override
   public Collection<Request> getChildren(Request request)
   {
      final Context context = request.getContext();

      final DependencyNodeImpl node = new DependencyNodeImpl();
      node.setDependency(request.getDependency());

      // apply dependency management
      final DependencyManagement dependencyManagement = context.getDependencyManagemer().manageDependency(
         node.getDependency());
      applyDependencyManagement(node, dependencyManagement, context.isSavePremanagedState(), false);

      // resolve version range
      final VersionRangeResult rangeResult;
      try
      {
         rangeResult = resolveVersionRange(context, node);
      }
      catch (VersionRangeResolutionException e)
      {
         addException(node.getDependency(), e);
         return Collections.emptyList();
      }

      // read artifact descriptor
      // handle cycle
      // handle relocation
      // traverse children (if traverse dependencies)

      // TODO determine children
      final List<Dependency> children = null;

      // TODO determine managedDependencies (from descriptor result)
      final Context childContext = deriveChildContext(context, node.getDependency(), (List<Dependency>) null);

      // TODO do we have to derive child selector first?
      final DependencySelector dependencySelector = context.getDependencySelector();

      final List<Request> childRequests = new ArrayList<Request>(children.size());
      for (Dependency child : children)
      {
         // if selectDependency
         if (!dependencySelector.selectDependency(child))
         {
            continue;
         }

         final Request childRequest = new Request();
         childRequest.setContext(childContext);
         childRequest.setDependency(child);
         childRequests.add(childRequest);
      }

      return childRequests;
   }

   private VersionRangeResult resolveVersionRange(final Context context, final DependencyNodeImpl node)
      throws VersionRangeResolutionException
   {
      // TODO cache
      return resolveVersionRange(session, context.getRequestContext(), context.getRequestTrace(),
         context.getRepositories(), node.getDependency());
   }

   private void addException(Dependency dependency, VersionRangeResolutionException e)
   {
      // TODO Auto-generated method stub

   }

   private VersionRangeResult resolveVersionRange(RepositorySystemSession session, String requestContext,
      RequestTrace trace, List<RemoteRepository> repositories, Dependency dependency)
      throws VersionRangeResolutionException
   {
      VersionRangeResult rangeResult;
      VersionRangeRequest rangeRequest = new VersionRangeRequest();
      rangeRequest.setArtifact(dependency.getArtifact());
      rangeRequest.setRepositories(repositories);
      rangeRequest.setRequestContext(requestContext);
      rangeRequest.setTrace(trace);

      rangeResult = versionRangeResolver.resolveVersionRange(session, rangeRequest);

      if (rangeResult.getVersions().isEmpty())
      {
         throw new VersionRangeResolutionException(rangeResult, "No versions available for " + dependency.getArtifact()
            + " within specified range");
      }
      return rangeResult;
   }

   private Context deriveChildContext(Context parentContext, Dependency dependency, List<Dependency> managedDependencies)
   {
      final DefaultDependencyCollectionContext collectionContext = new DefaultDependencyCollectionContext(session,
         dependency, managedDependencies);

      final Context childContext = new Context();

      childContext.setSavePremanagedState(parentContext.isSavePremanagedState());

      childContext.setDependencySelector(parentContext.getDependencySelector().deriveChildSelector(collectionContext));
      childContext.setDependencyManagemer(parentContext.getDependencyManagemer().deriveChildManager(collectionContext));
      childContext.setDependencyTraverser(parentContext.getDependencyTraverser()
         .deriveChildTraverser(collectionContext));

      return childContext;
   }

   private static void applyDependencyManagement(DependencyNodeImpl node, DependencyManagement dependencyManagement,
      boolean savePremanagedState, boolean disableVersionManagement)
   {
      Dependency dependency = node.getDependency();
      int managedBits = node.getManagedBits();

      final String managedVersion = dependencyManagement.getVersion();
      if (managedVersion != null && !disableVersionManagement)
      {
         final Artifact artifact = dependency.getArtifact();
         if (savePremanagedState)
         {
            node.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_VERSION, artifact.getVersion());
         }
         dependency = dependency.setArtifact(artifact.setVersion(managedVersion));
         managedBits |= DependencyNode.MANAGED_VERSION;
      }

      if (dependencyManagement.getProperties() != null)
      {
         Artifact artifact = dependency.getArtifact();
         dependency = dependency.setArtifact(artifact.setProperties(dependencyManagement.getProperties()));
         managedBits |= DependencyNode.MANAGED_PROPERTIES;
      }

      if (dependencyManagement.getScope() != null)
      {
         if (savePremanagedState)
         {
            node.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_SCOPE, dependency.getScope());
         }
         dependency = dependency.setScope(dependencyManagement.getScope());
         managedBits |= DependencyNode.MANAGED_SCOPE;
      }

      if (dependencyManagement.getOptional() != null)
      {
         if (savePremanagedState)
         {
            node.setData(DependencyManagerUtils.NODE_DATA_PREMANAGED_OPTIONAL, dependency.isOptional());
         }
         dependency = dependency.setOptional(dependencyManagement.getOptional());
         managedBits |= DependencyNode.MANAGED_OPTIONAL;
      }

      if (dependencyManagement.getExclusions() != null)
      {
         dependency = dependency.setExclusions(dependencyManagement.getExclusions());
         managedBits |= DependencyNode.MANAGED_EXCLUSIONS;
      }

      node.setDependency(dependency);
      node.setManagedBits(managedBits);
   }

   public static class DefaultBuilder
   {
      private final Stack<DependencyNode> nodes = new Stack<DependencyNode>();

      public void startDependency(Dependency dependency)
      {
         nodes.push(new DefaultDependencyNode(dependency));
      }

      public void setManagedVersion(String managedVersion)
      {

      }

      public void endDependency(Dependency dependency)
      {

      }
   }
}
