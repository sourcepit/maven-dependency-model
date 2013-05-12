/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.io.DefaultModelWriter;
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
import org.eclipse.emf.common.util.EList;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.util.FilterRepositorySystemSession;
import org.sonatype.aether.util.filter.ScopeDependencyFilter;
import org.sonatype.aether.util.graph.DefaultDependencyNode;
import org.sonatype.aether.util.graph.selector.StaticDependencySelector;
import org.sonatype.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.maven.model.util.MavenModelUtils;
import org.sourcepit.common.utils.lang.Exceptions;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyModelResolver;
import org.sourcepit.maven.dependency.model.aether.ReplaceRootNode;

@Named("aether")
public class AetherDependencyModelResolver implements DependencyModelResolver
{
   @Inject
   private LegacySupport buildContext;

   @Inject
   private ProjectBuilder projectBuilder;

   @Inject
   private ProjectDependenciesResolver dependenciesResolver;

   /**
    * {@inheritDoc}
    */
   @Override
   public DependencyModel resolve(@NotNull Collection<Dependency> dependencies) throws ProjectBuildingException,
      DependencyResolutionException
   {
      final Model model;

      final MavenProject currentProject = buildContext.getSession().getCurrentProject();
      if (currentProject == null)
      {
         model = new Model();
      }
      else
      {
         model = currentProject.getModel().clone();
      }

      model.setModelVersion("4.0.0");
      model.setGroupId("org.sourcepit");
      model.setArtifactId("dummy-project");
      model.setVersion("1337");

      model.getDependencies().clear();
      model.getDependencies().addAll(dependencies);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      try
      {
         new DefaultModelWriter().write(out, null, model);
      }
      catch (IOException e)
      {
         throw Exceptions.pipe(e);
      }
      final byte[] bytes = out.toByteArray();

      final ProjectBuildingRequest request = newProjectBuildingRequest(false, false);

      ProjectBuildingResult result = projectBuilder.build(new ModelSource()
      {
         @Override
         public String getLocation()
         {
            return "memory";
         }

         @Override
         public InputStream getInputStream() throws IOException
         {
            return new ByteArrayInputStream(bytes);
         }
      }, request);

      final MavenProject project = result.getProject();
      return resolve(project, false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DependencyModel resolve(@NotNull Artifact artifact) throws ProjectBuildingException,
      DependencyResolutionException
   {
      final MavenProject project = buildProject(artifact);
      return resolve(project);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DependencyModel resolve(@NotNull MavenProject project) throws DependencyResolutionException
   {
      return resolve(project, true);
   }

   private DependencyModel resolve(@NotNull MavenProject project, boolean resolveRoot)
      throws DependencyResolutionException
   {
      final DependencyModelBuilder modelBuilder = new DependencyModelBuilder();

      final DependencyGraphTransformer transformer;
      if (resolveRoot)
      {
         DefaultDependencyNode rootNode = new DefaultDependencyNode();
         rootNode.setDependency(new org.sonatype.aether.graph.Dependency(RepositoryUtils.toArtifact(project
            .getArtifact()), "compile"));
         rootNode.setRepositories(project.getRemoteProjectRepositories());
         rootNode.setRequestContext("project");

         transformer = new ChainedDependencyGraphTransformer(new ReplaceRootNode(rootNode),
            new DependencyModelBuildingGraphTransformer(modelBuilder, true, false));
      }
      else
      {
         transformer = new DependencyModelBuildingGraphTransformer(modelBuilder, true, false);
      }

      final RepositorySystemSession repositorySession = new FilterRepositorySystemSession(
         buildContext.getRepositorySession())
      {
         @Override
         public DependencySelector getDependencySelector()
         {
            return new StaticDependencySelector(true);
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer()
         {
            return transformer;
         }
      };

      final DefaultDependencyResolutionRequest resolutionRequest = new DefaultDependencyResolutionRequest();
      resolutionRequest.setMavenProject(project);
      resolutionRequest.setRepositorySession(repositorySession);
      resolutionRequest.setResolutionFilter(new ScopeDependencyFilter("test"));

      DependencyResolutionResult resolutionResult = dependenciesResolver.resolve(resolutionRequest);

      DependencyModel model = modelBuilder.getDependencyModel();

      applyResolvedArtifacts(project, resolutionResult, model);

      return model;
   }

   private void applyResolvedArtifacts(MavenProject project, DependencyResolutionResult resolutionResult,
      DependencyModel model)
   {
      Map<ArtifactKey, MavenArtifact> foo = new HashMap<ArtifactKey, MavenArtifact>();

      EList<MavenArtifact> artifacts2 = model.getArtifacts();
      for (MavenArtifact mavenArtifact : artifacts2)
      {
         foo.put(mavenArtifact.getArtifactKey(), mavenArtifact);
      }

      List<org.sonatype.aether.graph.Dependency> resolvedDependencies = resolutionResult.getResolvedDependencies();
      for (org.sonatype.aether.graph.Dependency dependency : resolvedDependencies)
      {
         org.sonatype.aether.artifact.Artifact artifact = dependency.getArtifact();
         final ArtifactKey artifactKey = MavenModelUtils.toArtifactKey(artifact);
         foo.get(artifactKey).setFile(artifact.getFile());
      }
   }

   private MavenProject buildProject(final Artifact artifact) throws ProjectBuildingException
   {
      final ProjectBuildingRequest request = newProjectBuildingRequest(false, false);

      ProjectBuildingResult build = projectBuilder.build(artifact, request);
      return build.getProject();
   }

   private ProjectBuildingRequest newProjectBuildingRequest(boolean resolveDeps, boolean processPlugins)
   {
      final ProjectBuildingRequest request = new DefaultProjectBuildingRequest(buildContext.getSession()
         .getProjectBuildingRequest());
      request.setResolveDependencies(resolveDeps);
      request.setProcessPlugins(processPlugins);
      request.setProject(null);

      final MavenProject project = buildContext.getSession().getCurrentProject();
      if (project != null)
      {
         @SuppressWarnings("unchecked")
         List<ArtifactRepository> artifactRepos = combine(project.getRemoteArtifactRepositories(),
            request.getRemoteRepositories());

         @SuppressWarnings("unchecked")
         List<ArtifactRepository> pluginRepos = combine(project.getPluginArtifactRepositories(),
            request.getPluginArtifactRepositories());

         request.setRemoteRepositories(artifactRepos);
         request.setPluginArtifactRepositories(pluginRepos);
      }

      return request;
   }

   private static List<ArtifactRepository> combine(List<ArtifactRepository>... repoLists)
   {
      final Set<String> ids = new HashSet<String>();
      final List<ArtifactRepository> result = new ArrayList<ArtifactRepository>();

      for (List<ArtifactRepository> repos : repoLists)
      {
         for (ArtifactRepository repo : repos)
         {
            if (ids.add(repo.getId()))
            {
               result.add(repo);
            }
         }
      }

      return result;
   }
}
