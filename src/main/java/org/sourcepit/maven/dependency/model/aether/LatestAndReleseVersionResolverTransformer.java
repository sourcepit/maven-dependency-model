/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import javax.validation.constraints.NotNull;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.resolution.VersionRequest;
import org.sonatype.aether.resolution.VersionResolutionException;
import org.sonatype.aether.resolution.VersionResult;
import org.sourcepit.common.maven.model.util.MavenModelUtils;
import org.sourcepit.common.utils.lang.Exceptions;
import org.sourcepit.common.utils.lang.PipedException;

public class LatestAndReleseVersionResolverTransformer implements DependencyGraphTransformer
{
   private final VersionResolver versionResolver;

   public LatestAndReleseVersionResolverTransformer(@NotNull VersionResolver versionResolver)
   {
      this.versionResolver = versionResolver;
   }

   @Override
   public DependencyNode transformGraph(final DependencyNode node, final DependencyGraphTransformationContext context)
      throws RepositoryException
   {
      final DependencyVisitor visitor = new AbstractDependencyVisitor(false)
      {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            final Artifact artifact = getArtifact(node);
            if (artifact != null)
            {
               final String version = artifact.getVersion();
               if ("LATEST".equals(version) || "RELEASE".equals(version))
               {
                  final VersionRequest request = newVersionRequest(node, artifact);

                  final VersionResult result;
                  try
                  {
                     result = versionResolver.resolveVersion(context.getSession(), request);
                  }
                  catch (VersionResolutionException e)
                  {
                     throw Exceptions.pipe(e);
                  }
                  node.setArtifact(artifact.setVersion(MavenModelUtils.normalizeSnapshotVersion(result.getVersion())));
               }
            }
            return super.onVisitEnter(parent, node);
         }
      };
      try
      {
         node.accept(visitor);
      }
      catch (PipedException e)
      {
         e.adaptAndThrow(VersionResolutionException.class);
         throw e;
      }

      return node;
   }

   private static VersionRequest newVersionRequest(DependencyNode node, final Artifact artifact)
   {
      return new VersionRequest(artifact, node.getRepositories(), node.getRequestContext());
   }

   private static Artifact getArtifact(DependencyNode node)
   {
      final Dependency dependency = node.getDependency();
      if (dependency != null)
      {
         return dependency.getArtifact();
      }
      return null;
   }
}
