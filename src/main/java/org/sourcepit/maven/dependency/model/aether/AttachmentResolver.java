/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sourcepit.common.utils.lang.PipedException;

@Named
public class AttachmentResolver
{
   private ArtifactResolver artifactResolver;

   @Inject
   public AttachmentResolver(ArtifactResolver artifactResolver)
   {
      this.artifactResolver = artifactResolver;
   }

   public Collection<Artifact> resolveAttachments(final RepositorySystemSession repositorySession, DependencyNode graph)
      throws ArtifactResolutionException
   {
      final Set<Artifact> resolvedAttachments = new LinkedHashSet<Artifact>();
      final DependencyVisitor attachmentResolver = new AbstractDependencyVisitor(false)
      {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            try
            {
               resolveAttachments(resolvedAttachments, repositorySession, node);
            }
            catch (ArtifactResolutionException e)
            {
               throw pipe(e);
            }
            return super.onVisitEnter(parent, node);
         }
      };

      try
      {
         graph.accept(attachmentResolver);
      }
      catch (PipedException e)
      {
         e.adaptAndThrow(ArtifactResolutionException.class);
         throw e;
      }

      return resolvedAttachments;
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void resolveAttachments(Collection<Artifact> resolvedAttachments,
      final RepositorySystemSession repositorySession, DependencyNode node) throws ArtifactResolutionException
   {
      Collection<Artifact> attachments;

      // resolve required attachments
      attachments = (Collection) node.getData().get("requiredAttachments");
      if (attachments != null && !attachments.isEmpty())
      {
         resolveAttachments(resolvedAttachments, repositorySession, node, attachments, true);
      }

      // resolve optional attachments
      attachments = (Collection) node.getData().get("optionalAttachments");
      if (attachments != null && !attachments.isEmpty())
      {
         resolveAttachments(resolvedAttachments, repositorySession, node, attachments, false);
      }
   }

   private void resolveAttachments(Collection<Artifact> resolvedAttachments, RepositorySystemSession session,
      DependencyNode node, Collection<Artifact> attachments, boolean required) throws ArtifactResolutionException
   {
      final List<ArtifactRequest> requests = new ArrayList<ArtifactRequest>(attachments.size());

      for (org.sonatype.aether.artifact.Artifact artifact : attachments)
      {
         requests.add(new ArtifactRequest(artifact, node.getRepositories(), node.getRequestContext()));
      }

      List<ArtifactResult> results = null;
      try
      {
         results = artifactResolver.resolveArtifacts(session, requests);
      }
      catch (ArtifactResolutionException e)
      {
         results = e.getResults();
         if (required)
         {
            throw e;
         }
      }

      for (ArtifactResult result : results)
      {
         if (result.isResolved())
         {
            resolvedAttachments.add(result.getArtifact());
         }
      }
   }

}
