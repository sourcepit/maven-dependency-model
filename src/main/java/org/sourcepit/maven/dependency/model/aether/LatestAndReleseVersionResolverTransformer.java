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
