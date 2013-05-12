/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.impl;

import javax.validation.constraints.NotNull;

import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.maven.dependency.model.DependencyNode;

public final class DependencyNodeOperations
{
   private DependencyNodeOperations()
   {
      super();
   }

   public static String getGroupId(@NotNull DependencyNode n)
   {
      return n.getDeclaredDependency().getGroupId();
   }

   public static String getArtifactId(@NotNull DependencyNode n)
   {
      return n.getDeclaredDependency().getArtifactId();
   }

   public static String getClassifier(@NotNull DependencyNode n)
   {
      return n.getDeclaredDependency().getClassifier();
   }

   public static String getType(@NotNull DependencyNode n)
   {
      return n.getDeclaredDependency().getType();
   }

   public static Scope getEffectiveScope(@NotNull DependencyNode n)
   {
      Scope scope = n.getManagedScope();
      if (scope == null)
      {
         scope = n.getInheritedScope();
         if (scope == null)
         {
            scope = n.getDeclaredDependency().getScope();
         }
      }
      return scope;
   }

   public static String getEffectiveVersionConstraint(@NotNull DependencyNode n)
   {
      String versionConstraint;
      versionConstraint = n.getManagedVersionConstraint();
      if (versionConstraint != null)
      {
         return versionConstraint;
      }

      versionConstraint = n.getConflictVersionConstraint();
      if (versionConstraint != null)
      {
         return versionConstraint;
      }

      return n.getDeclaredDependency().getVersionConstraint();
   }
}
