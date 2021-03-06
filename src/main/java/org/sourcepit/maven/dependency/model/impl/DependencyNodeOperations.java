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

package org.sourcepit.maven.dependency.model.impl;

import org.sourcepit.common.constraints.NotNull;
import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.maven.dependency.model.DependencyNode;

public final class DependencyNodeOperations {
   private DependencyNodeOperations() {
      super();
   }

   public static String getGroupId(@NotNull DependencyNode n) {
      return n.getDeclaredDependency().getGroupId();
   }

   public static String getArtifactId(@NotNull DependencyNode n) {
      return n.getDeclaredDependency().getArtifactId();
   }

   public static String getClassifier(@NotNull DependencyNode n) {
      return n.getDeclaredDependency().getClassifier();
   }

   public static String getType(@NotNull DependencyNode n) {
      return n.getDeclaredDependency().getType();
   }

   public static Scope getEffectiveScope(@NotNull DependencyNode n) {
      Scope scope = n.getManagedScope();
      if (scope == null) {
         scope = n.getInheritedScope();
         if (scope == null) {
            scope = n.getDeclaredDependency().getScope();
         }
      }
      return scope;
   }

   public static String getEffectiveVersionConstraint(@NotNull DependencyNode n) {
      String versionConstraint;
      versionConstraint = n.getManagedVersionConstraint();
      if (versionConstraint != null) {
         return versionConstraint;
      }

      versionConstraint = n.getConflictVersionConstraint();
      if (versionConstraint != null) {
         return versionConstraint;
      }

      return n.getDeclaredDependency().getVersionConstraint();
   }
}
