/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import org.sourcepit.common.constraints.NotNull;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.resolution.VersionRequest;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;
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
