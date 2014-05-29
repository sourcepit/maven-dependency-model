/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.collection.DependencyManagement;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

@Named
public class DescriptorResolverImpl implements DescriptorResolver
{
   private final VersionRangeResolver versionRangeResolver;

   private final ArtifactDescriptorReader descriptorReader;

   @Inject
   public DescriptorResolverImpl(VersionRangeResolver versionRangeResolver, ArtifactDescriptorReader descriptorReader)
   {
      this.descriptorReader = descriptorReader;
      this.versionRangeResolver = versionRangeResolver;
   }

   @Override
   public DescriptorResolutionResult resolveDescriptors(DescriptorResolutionRequest request)
   {
      return resolveDependency(request.getDependency(), null, false, request.getSession(),
         request.getDependencyManager(), request.getDependencySelector(), request.getRequestContext(),
         request.getRequestTrace(), request.getRepositories());
   }

   private DescriptorResolutionResult resolveDependency(Dependency dependency, List<Artifact> relocations,
      boolean disableVersionManagement, RepositorySystemSession session, DependencyManager dependencyManager,
      DependencySelector dependencySelector, String requestContext, RequestTrace trace,
      List<RemoteRepository> repositories)
   {
      DescriptorResolutionResult result = new DescriptorResolutionResult();

      // apply dependency management
      result.setManagedDependency(applyDependencyManagement2(dependency, dependencyManager, disableVersionManagement));

      final Dependency managedDependency = result.getManagedDependency().getDependency();

      // resolve version range
      final VersionRangeResult rangeResult;
      rangeResult = resolveVersionRange(session, requestContext, trace, repositories, managedDependency);
      result.setVersionRangeResult(rangeResult);

      for (final Version version : rangeResult.getVersions())
      {
         try
         {
            final ArtifactDescriptorResult descriptorResult = readArtifactDescriptor(session, requestContext, trace,
               repositories, managedDependency.getArtifact().setVersion(version.toString()));

            result.getArtifactDescriptorResults().put(version, descriptorResult);

            if (descriptorResult.getRelocations().isEmpty())
            {
               descriptorResult.setRelocations(relocations);
            }
            else
            {
               final Artifact originalArtifact = managedDependency.getArtifact();
               final Artifact currentArtifact = descriptorResult.getArtifact();

               if (relocations != null && relocations.contains(currentArtifact))
               {
                  return result;
               }

               disableVersionManagement = originalArtifact.getGroupId().equals(currentArtifact.getGroupId())
                  && originalArtifact.getArtifactId().equals(currentArtifact.getArtifactId());

               final Dependency relocatedDependency = managedDependency.setArtifact(currentArtifact);
               if (!dependencySelector.selectDependency(relocatedDependency))
               {
                  return null;
               }

               return resolveDependency(relocatedDependency, descriptorResult.getRelocations(),
                  disableVersionManagement, session, dependencyManager, dependencySelector, requestContext, trace,
                  repositories);
            }
         }
         catch (ArtifactDescriptorException e)
         {
            result.getArtifactDescriptorResults().put(version, e.getResult());
         }
      }
      return result;
   }

   private ManagedDependency applyDependencyManagement2(Dependency dependency, DependencyManager dependencyManager,
      boolean disableVersionManagement)
   {
      final DependencyManagement dependencyManagement = dependencyManager.manageDependency(dependency);
      if (dependencyManagement != null)
      {
         return applyDependencyManagement2(dependency, dependencyManagement, disableVersionManagement);
      }

      final ManagedDependency result = new ManagedDependency();
      result.setDependency(dependency);
      return result;
   }

   private static ManagedDependency applyDependencyManagement2(Dependency dependency,
      DependencyManagement dependencyManagement, boolean disableVersionManagement)
   {
      final ManagedDependency result = new ManagedDependency();

      int managedBits = result.getManagedBits();

      final String managedVersion = dependencyManagement.getVersion();
      if (managedVersion != null && !disableVersionManagement)
      {
         final Artifact artifact = dependency.getArtifact();
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
         dependency = dependency.setScope(dependencyManagement.getScope());
         managedBits |= DependencyNode.MANAGED_SCOPE;
      }

      if (dependencyManagement.getOptional() != null)
      {
         dependency = dependency.setOptional(dependencyManagement.getOptional());
         managedBits |= DependencyNode.MANAGED_OPTIONAL;
      }

      if (dependencyManagement.getExclusions() != null)
      {
         dependency = dependency.setExclusions(dependencyManagement.getExclusions());
         managedBits |= DependencyNode.MANAGED_EXCLUSIONS;
      }

      result.setDependency(dependency);
      result.setManagedBits(managedBits);

      return result;
   }

   private VersionRangeResult resolveVersionRange(RepositorySystemSession session, String requestContext,
      RequestTrace trace, List<RemoteRepository> repositories, Dependency dependency)
   {
      final VersionRangeRequest rangeRequest = new VersionRangeRequest();
      rangeRequest.setArtifact(dependency.getArtifact());
      rangeRequest.setRepositories(repositories);
      rangeRequest.setRequestContext(requestContext);
      rangeRequest.setTrace(trace);

      try
      {
         return versionRangeResolver.resolveVersionRange(session, rangeRequest);
      }
      catch (VersionRangeResolutionException e)
      {
         return e.getResult();
      }
   }

   private boolean isLackingDescriptor(Artifact artifact)
   {
      return artifact.getProperty(ArtifactProperties.LOCAL_PATH, null) != null;
   }

   private ArtifactDescriptorResult readArtifactDescriptor(RepositorySystemSession session, String requestContext,
      RequestTrace trace, List<RemoteRepository> repositories, Artifact artifact) throws ArtifactDescriptorException
   {
      ArtifactDescriptorResult descriptorResult;
      ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
      descriptorRequest.setArtifact(artifact);
      descriptorRequest.setRepositories(repositories);
      descriptorRequest.setRequestContext(requestContext);
      descriptorRequest.setTrace(trace);

      if (isLackingDescriptor(artifact))
      {
         descriptorResult = new ArtifactDescriptorResult(descriptorRequest);
      }
      else
      {
         // TODO cache
         // TODO externalize

         descriptorResult = descriptorReader.readArtifactDescriptor(session, descriptorRequest);

      }
      return descriptorResult;
   }
}
