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

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.sourcepit.common.utils.lang.PipedException;

@Named
public class AttachmentResolver {
   private ArtifactResolver artifactResolver;

   @Inject
   public AttachmentResolver(ArtifactResolver artifactResolver) {
      this.artifactResolver = artifactResolver;
   }

   public Collection<Artifact> resolveAttachments(final RepositorySystemSession repositorySession, DependencyNode graph)
      throws ArtifactResolutionException {
      final Set<Artifact> resolvedAttachments = new LinkedHashSet<Artifact>();
      final DependencyVisitor attachmentResolver = new AbstractDependencyVisitor(false) {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node) {
            try {
               resolveAttachments(resolvedAttachments, repositorySession, node);
            }
            catch (ArtifactResolutionException e) {
               throw pipe(e);
            }
            return super.onVisitEnter(parent, node);
         }
      };

      try {
         graph.accept(attachmentResolver);
      }
      catch (PipedException e) {
         e.adaptAndThrow(ArtifactResolutionException.class);
         throw e;
      }

      return resolvedAttachments;
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void resolveAttachments(Collection<Artifact> resolvedAttachments,
      final RepositorySystemSession repositorySession, DependencyNode node) throws ArtifactResolutionException {
      Collection<Artifact> attachments;

      // resolve required attachments
      attachments = (Collection) node.getData().get("requiredAttachments");
      if (attachments != null && !attachments.isEmpty()) {
         resolveAttachments(resolvedAttachments, repositorySession, node, attachments, true);
      }

      // resolve optional attachments
      attachments = (Collection) node.getData().get("optionalAttachments");
      if (attachments != null && !attachments.isEmpty()) {
         resolveAttachments(resolvedAttachments, repositorySession, node, attachments, false);
      }
   }

   private void resolveAttachments(Collection<Artifact> resolvedAttachments, RepositorySystemSession session,
      DependencyNode node, Collection<Artifact> attachments, boolean required) throws ArtifactResolutionException {
      final List<ArtifactRequest> requests = new ArrayList<ArtifactRequest>(attachments.size());

      for (org.eclipse.aether.artifact.Artifact artifact : attachments) {
         requests.add(new ArtifactRequest(artifact, node.getRepositories(), node.getRequestContext()));
      }

      List<ArtifactResult> results = null;
      try {
         results = artifactResolver.resolveArtifacts(session, requests);
      }
      catch (ArtifactResolutionException e) {
         results = e.getResults();
         if (required) {
            throw e;
         }
      }

      for (ArtifactResult result : results) {
         if (result.isResolved()) {
            resolvedAttachments.add(result.getArtifact());
         }
      }
   }

}
