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

package org.sourcepit.maven.dependency.model.aether;

import static org.apache.maven.RepositoryUtils.toRepo;
import static org.sourcepit.common.maven.artifact.MavenArtifactUtils.toArtifactKey;
import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

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
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.RepositoryConnectorProvider;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.transfer.NoRepositoryConnectorException;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.eclipse.emf.common.util.EList;
import org.sourcepit.common.constraints.NotNull;
import org.sourcepit.common.maven.artifact.ArtifactFactory;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.utils.lang.Exceptions;
import org.sourcepit.maven.dependency.model.ArtifactAttachmentFactory;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyModelResolver;

@Named("aether")
public class AetherDependencyModelResolver implements DependencyModelResolver {
   @Inject
   private ArtifactFactory artifactFactory;

   @Inject
   private AttachmentResolver attachmentResolver;

   @Inject
   private LegacySupport buildContext;

   @Inject
   private ProjectBuilder projectBuilder;

   @Inject
   private ProjectDependenciesResolver dependenciesResolver;

   @Inject
   private RepositoryConnectorProvider repositoryConnectorProvider;

   /**
    * {@inheritDoc}
    */
   @Override
   public DependencyModel resolve(@NotNull Collection<Dependency> dependencies,
      ArtifactAttachmentFactory attachmentFactory) throws ProjectBuildingException, DependencyResolutionException {
      final Model model;

      final MavenProject currentProject = buildContext.getSession().getCurrentProject();
      if (currentProject == null) {
         model = new Model();
      }
      else {
         model = currentProject.getModel().clone();
      }

      model.setModelVersion("4.0.0");
      model.setGroupId("org.sourcepit");
      model.setArtifactId("dummy-project");
      model.setVersion("1337");

      model.getDependencies().clear();
      model.getDependencies().addAll(dependencies);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
         new DefaultModelWriter().write(out, null, model);
      }
      catch (IOException e) {
         throw Exceptions.pipe(e);
      }
      final byte[] bytes = out.toByteArray();

      final ProjectBuildingRequest request = newProjectBuildingRequest(false, false);

      ProjectBuildingResult result = projectBuilder.build(new ModelSource() {
         @Override
         public String getLocation() {
            return "memory";
         }

         @Override
         public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(bytes);
         }
      }, request);

      final MavenProject project = result.getProject();
      return resolve(project, false, attachmentFactory);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DependencyModel resolve(@NotNull Artifact artifact, ArtifactAttachmentFactory attachmentFactory)
      throws ProjectBuildingException, DependencyResolutionException {
      final MavenProject project = buildProject(artifact);
      return resolve(project, attachmentFactory);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DependencyModel resolve(@NotNull MavenProject project, ArtifactAttachmentFactory attachmentFactory)
      throws DependencyResolutionException {
      return resolve(project, true, attachmentFactory);
   }

   @Inject
   private VersionResolver versionResolver;

   private DependencyModel resolve(@NotNull MavenProject project, boolean resolveRoot,
      ArtifactAttachmentFactory attachmentFactory) throws DependencyResolutionException {
      project = filterUnconnectableRepos(project);

      final DependencyModelBuilder modelBuilder = new DependencyModelBuilder(attachmentFactory);

      final RepositorySystemSession repositorySession = newRepositorySystemSession(project, resolveRoot, modelBuilder);

      final DefaultDependencyResolutionRequest resolutionRequest = new DefaultDependencyResolutionRequest();
      resolutionRequest.setMavenProject(project);
      resolutionRequest.setRepositorySession(repositorySession);
      resolutionRequest.setResolutionFilter(new ScopeDependencyFilter("test"));

      DependencyResolutionResult resolutionResult;
      try {
         resolutionResult = dependenciesResolver.resolve(resolutionRequest);
      }
      catch (DependencyResolutionException e) {
         resolutionResult = e.getResult();
      }

      final Collection<org.eclipse.aether.artifact.Artifact> resolvedAttachments;
      try {
         resolvedAttachments = attachmentResolver.resolveAttachments(repositorySession,
            resolutionResult.getDependencyGraph());
      }
      catch (ArtifactResolutionException e) {
         throw pipe(e);
      }

      final DependencyModel model = modelBuilder.getDependencyModel();

      applyResolvedArtifacts(project, resolutionResult, resolvedAttachments, model);

      final DependencyNode dependencyGraph = resolutionResult.getDependencyGraph();
      if (resolveRoot) {
         addNodeArtifact(model.getRootArtifacts(), model, dependencyGraph);
      }
      else {
         for (DependencyNode dependencyNode : dependencyGraph.getChildren()) {
            addNodeArtifact(model.getRootArtifacts(), model, dependencyNode);
         }
      }

      return model;
   }

   private MavenProject filterUnconnectableRepos(MavenProject project) {
      final RepositorySystemSession repositorySession = buildContext.getRepositorySession();

      final List<ArtifactRepository> repositories = project.getRemoteArtifactRepositories();
      final List<ArtifactRepository> validRepositories = new ArrayList<ArtifactRepository>();
      final List<ArtifactRepository> invalidRepositories = new ArrayList<ArtifactRepository>();
      for (ArtifactRepository artifactRepository : repositories) {
         try {
            repositoryConnectorProvider.newRepositoryConnector(repositorySession, toRepo(artifactRepository));
            validRepositories.add(artifactRepository);
         }
         catch (NoRepositoryConnectorException e) {
            invalidRepositories.add(artifactRepository);
         }
      }

      if (!invalidRepositories.isEmpty()) {
         project = project.clone();
         project.setRemoteArtifactRepositories(validRepositories);
      }

      return project;
   }

   private void addNodeArtifact(List<MavenArtifact> roots, DependencyModel model, DependencyNode dependencyNode) {
      MavenArtifact artifact = model.getArtifact(toArtifactKey(dependencyNode.getDependency().getArtifact()));
      roots.add(artifact);
   }


   private RepositorySystemSession newRepositorySystemSession(MavenProject project, boolean resolveRoot,
      final DependencyModelBuilder modelBuilder) {
      final List<DependencyGraphTransformer> transformers = new ArrayList<DependencyGraphTransformer>(3);

      if (resolveRoot) {
         org.eclipse.aether.graph.Dependency dependency = new org.eclipse.aether.graph.Dependency(
            RepositoryUtils.toArtifact(project.getArtifact()), "compile");
         DefaultDependencyNode rootNode = new DefaultDependencyNode(dependency);
         rootNode.setRepositories(project.getRemoteProjectRepositories());
         rootNode.setRequestContext("project");

         transformers.add(new ReplaceRootNode(rootNode));
      }

      transformers.add(new LatestAndReleseVersionResolverTransformer(versionResolver));
      transformers.add(new DependencyModelBuildingGraphTransformer(artifactFactory, modelBuilder, false, false));

      final DependencyGraphTransformer transformer = new ChainedDependencyGraphTransformer(
         transformers.toArray(new DependencyGraphTransformer[transformers.size()]));

      final RepositorySystemSession repositorySession = new AbstractForwardingRepositorySystemSession() {
         @Override
         public DependencySelector getDependencySelector() {
            return new ScopeChildDependenciesErasure(Collections.singleton("test"));
         }

         @Override
         public DependencyGraphTransformer getDependencyGraphTransformer() {
            return transformer;
         }

         @Override
         protected RepositorySystemSession getSession() {
            return buildContext.getRepositorySession();
         }
      };
      return repositorySession;
   }

   private void applyResolvedArtifacts(MavenProject project, DependencyResolutionResult resolutionResult,
      Collection<org.eclipse.aether.artifact.Artifact> resolvedAttachments, DependencyModel model) {
      Map<ArtifactKey, MavenArtifact> keyToArtifact = new HashMap<ArtifactKey, MavenArtifact>();

      EList<MavenArtifact> artifacts2 = model.getArtifacts();
      for (MavenArtifact mavenArtifact : artifacts2) {
         keyToArtifact.put(mavenArtifact.getArtifactKey(), mavenArtifact);
      }

      for (org.eclipse.aether.artifact.Artifact artifact : resolvedAttachments) {
         final ArtifactKey artifactKey = toArtifactKey(artifact);
         keyToArtifact.get(artifactKey).setFile(artifact.getFile());
      }

      for (org.eclipse.aether.graph.Dependency dependency : resolutionResult.getResolvedDependencies()) {
         org.eclipse.aether.artifact.Artifact artifact = dependency.getArtifact();
         final ArtifactKey artifactKey = toArtifactKey(artifact);
         keyToArtifact.get(artifactKey).setFile(artifact.getFile());
      }
   }

   private MavenProject buildProject(final Artifact artifact) throws ProjectBuildingException {
      final ProjectBuildingRequest request = newProjectBuildingRequest(false, false);

      ProjectBuildingResult build = projectBuilder.build(artifact, request);
      return build.getProject();
   }

   private ProjectBuildingRequest newProjectBuildingRequest(boolean resolveDeps, boolean processPlugins) {
      final ProjectBuildingRequest request = new DefaultProjectBuildingRequest(buildContext.getSession()
         .getProjectBuildingRequest());
      request.setResolveDependencies(resolveDeps);
      request.setProcessPlugins(processPlugins);
      request.setProject(null);

      final MavenProject project = buildContext.getSession().getCurrentProject();
      if (project != null) {
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

   private static List<ArtifactRepository> combine(List<ArtifactRepository>... repoLists) {
      final Set<String> ids = new HashSet<String>();
      final List<ArtifactRepository> result = new ArrayList<ArtifactRepository>();

      for (List<ArtifactRepository> repos : repoLists) {
         for (ArtifactRepository repo : repos) {
            if (ids.add(repo.getId())) {
               result.add(repo);
            }
         }
      }

      return result;
   }
}
