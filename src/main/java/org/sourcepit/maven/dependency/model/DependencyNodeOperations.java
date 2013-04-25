/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import javax.validation.constraints.NotNull;

import org.sourcepit.common.maven.model.Scope;

public final class DependencyNodeOperations
{
   private DependencyNodeOperations()
   {
      super();
   }

   public static String getGroupId(@NotNull DependencyNode n)
   {
      return getGroupId(n.getDependencyDeclaration());
   }

   private static String getGroupId(@NotNull DeclaredDependency declaration)
   {
      return declaration.getGroupId();
   }
   
   public static String getArtifactId(@NotNull DependencyNode n)
   {
      return getArtifactId(n.getDependencyDeclaration());
   }

   private static String getArtifactId(@NotNull DeclaredDependency declaration)
   {
      return declaration.getArtifactId();
   }

   public static Scope getEffectiveScope(DependencyNode n)
   {
      Scope scope = n.getManagedScope();
      if (scope == null)
      {
         scope = n.getInheritedScope();
         if (scope == null)
         {
            scope = n.getDependencyDeclaration().getScope();
         }
      }
      return scope;
   }
}
