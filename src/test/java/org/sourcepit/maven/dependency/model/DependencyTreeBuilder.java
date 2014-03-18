/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.eclipse.aether.util.graph.transformer.JavaDependencyContextRefiner;
import org.sourcepit.maven.dependency.model.aether.DependencyNode2AdapterTransformer;
import org.sourcepit.maven.dependency.model.aether.NearestDependencyNodeChooser;
import org.sourcepit.maven.dependency.model.aether.VersionConflictResolver;

@Named
public class DependencyTreeBuilder
{
   private final LegacySupport buildContext;

   @Inject
   private ProjectDependenciesResolver resolver;

   @Inject
   private ProjectBuilder projectBuilder;

   @Inject
   public DependencyTreeBuilder(LegacySupport buildContext)
   {
      this.buildContext = buildContext;
   }

   public DependencyNode build(DependencyTreeBuilderRequest request) throws ProjectBuildingException,
      DependencyResolutionException
   {
      DependencyResolutionResult resolutionResult = resolver.resolve(buildDependencyResolutionRequest(request));

      return resolutionResult.getDependencyGraph();
   }

   private DefaultDependencyResolutionRequest buildDependencyResolutionRequest(DependencyTreeBuilderRequest request)
      throws ProjectBuildingException
   {
      final MavenProject project = buildProject(request.getArtifact());

      final DependencySelector selector = buildDependencySelector(request);

      final DependencyGraphTransformer transformer = new ChainedDependencyGraphTransformer(
         new DependencyNode2AdapterTransformer(false), new VersionConflictResolver(new NearestDependencyNodeChooser(false)),
         new JavaDependencyContextRefiner());

      final RepositorySystemSession repositorySession = new AbstractForwardingRepositorySystemSession()
      {
         @Override
         public DependencySelector getDependencySelector()
         {
            return selector;
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer()
         {
            return transformer;
         }

         @Override
         protected RepositorySystemSession getSession()
         {
            return buildContext.getRepositorySession();
         }
      };

      final DefaultDependencyResolutionRequest resolutionRequest = new DefaultDependencyResolutionRequest();
      resolutionRequest.setMavenProject(project);
      resolutionRequest.setRepositorySession(repositorySession);
      resolutionRequest.setResolutionFilter(new ReplacedDependencyFilter());

      resolutionRequest.setResolutionFilter(new ScopeDependencyFilter("test"));

      return resolutionRequest;
   }


   private DependencySelector buildDependencySelector(DependencyTreeBuilderRequest request)
   {
      final Set<DependencySelector> dependencySelectors = new LinkedHashSet<DependencySelector>(3);
      // dependencySelectors.add(new ScopeErasure("test"));
      if (request.isExcludeOptionalDependencies())
      {
         dependencySelectors.add(new OptionalErasure());
      }
      dependencySelectors.add(new ExclusionDependencySelector());

      return new AndDependencySelector(dependencySelectors);
   }

   private MavenProject buildProject(final Artifact artifact) throws ProjectBuildingException
   {
      ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
      request.setResolveDependencies(false);
      request.setProcessPlugins(false);

      MavenSession session = buildContext.getSession();

      request.setRepositorySession(session.getRepositorySession());
      request.setLocalRepository(session.getLocalRepository());
      request.setSystemProperties(session.getSystemProperties());
      request.setUserProperties(request.getUserProperties());

      MavenExecutionRequest executionRequest = session.getRequest();
      request.setRemoteRepositories(executionRequest.getRemoteRepositories());
      request.setPluginArtifactRepositories(executionRequest.getPluginArtifactRepositories());

      ProjectBuildingResult build = projectBuilder.build(artifact, request);

      return build.getProject();
   }
}
