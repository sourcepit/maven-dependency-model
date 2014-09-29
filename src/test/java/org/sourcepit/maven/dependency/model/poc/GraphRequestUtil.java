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

package org.sourcepit.maven.dependency.model.poc;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.artifact.ArtifactIdUtils;
import org.eclipse.aether.util.artifact.JavaScopes;

public final class GraphRequestUtil
{
   private GraphRequestUtil()
   {
      super();
   }

   public static GraphRequest newGraphRequest(final RepositorySystemSession session, final MavenProject project,
      DependencyResolutionScope dependencyResolutionScope)
   {
      final GraphRequest request = new GraphRequest();
      fillGraphRequest(session, project, dependencyResolutionScope, request);
      return request;
   }

   public static GraphsRequest newGraphsRequest(final RepositorySystemSession session, final MavenProject project,
      DependencyResolutionScope dependencyResolutionScope, ConflictResolutionScope conflictResolutionScope)
   {
      final GraphsRequest request = new GraphsRequest();
      request.setConflictResolutionScope(conflictResolutionScope);
      fillGraphRequest(session, project, dependencyResolutionScope, request);
      return request;
   }

   private static void fillGraphRequest(final RepositorySystemSession session, final MavenProject project,
      DependencyResolutionScope dependencyResolutionScope, final GraphRequest request)
   {
      request.setDependencyResolutionScope(dependencyResolutionScope);
      setArtifact(project, request);
      addDependencies(session, project, request.getDependencies());
      addManagedDependencies(session, project, request.getManagedDependencies());
      addRepositories(project, request.getRepositories());
   }

   private static void setArtifact(final MavenProject project, final GraphRequest request)
   {
      request.setRootArtifact(RepositoryUtils.toArtifact(project.getArtifact()));
   }

   private static void addRepositories(final MavenProject project, List<RemoteRepository> repositories)
   {
      repositories.addAll(project.getRemoteProjectRepositories());
   }

   private static void addManagedDependencies(final RepositorySystemSession session, final MavenProject project,
      List<org.eclipse.aether.graph.Dependency> managedDeps)
   {
      final ArtifactTypeRegistry stereotypes = session.getArtifactTypeRegistry();
      DependencyManagement depMngt = project.getDependencyManagement();
      if (depMngt != null)
      {
         for (Dependency dependency : depMngt.getDependencies())
         {

            managedDeps.add(RepositoryUtils.toDependency(dependency, stereotypes));
         }
      }
   }

   private static void addDependencies(final RepositorySystemSession session, final MavenProject project,
      List<org.eclipse.aether.graph.Dependency> deps)
   {
      final ArtifactTypeRegistry stereotypes = session.getArtifactTypeRegistry();
      if (project.getDependencyArtifacts() == null)
      {
         for (Dependency dependency : project.getDependencies())
         {
            if (isNullOrEmpty(dependency.getGroupId()) || isNullOrEmpty(dependency.getArtifactId())
               || isNullOrEmpty(dependency.getVersion()))
            {
               // guard against case where best-effort resolution for invalid models is requested
               continue;
            }
            deps.add(RepositoryUtils.toDependency(dependency, stereotypes));
         }
      }
      else
      {
         Map<String, Dependency> dependencies = new HashMap<String, Dependency>();
         for (Dependency dependency : project.getDependencies())
         {
            String classifier = dependency.getClassifier();
            if (classifier == null)
            {
               ArtifactType type = stereotypes.get(dependency.getType());
               if (type != null)
               {
                  classifier = type.getClassifier();
               }
            }
            String key = ArtifactIdUtils.toVersionlessId(dependency.getGroupId(), dependency.getArtifactId(),
               dependency.getType(), classifier);
            dependencies.put(key, dependency);
         }
         for (Artifact artifact : project.getDependencyArtifacts())
         {
            String key = artifact.getDependencyConflictId();
            Dependency dependency = dependencies.get(key);
            Collection<Exclusion> exclusions = dependency != null ? dependency.getExclusions() : null;
            org.eclipse.aether.graph.Dependency dep = RepositoryUtils.toDependency(artifact, exclusions);
            if (!JavaScopes.SYSTEM.equals(dep.getScope()) && dep.getArtifact().getFile() != null)
            {
               // enable re-resolution
               org.eclipse.aether.artifact.Artifact art = dep.getArtifact();
               art = art.setFile(null).setVersion(art.getBaseVersion());
               dep = dep.setArtifact(art);
            }
            deps.add(dep);
         }
      }
   }


}
