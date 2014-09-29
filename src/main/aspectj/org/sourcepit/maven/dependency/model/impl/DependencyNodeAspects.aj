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

import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.maven.dependency.model.DependencyNode;
import org.sourcepit.maven.dependency.model.impl.DependencyNodeOperations;


public aspect DependencyNodeAspects
{
   pointcut getGroupId(DependencyNode n): target(n) && args() && execution(String getGroupId());

   String around(DependencyNode n) : getGroupId(n){
      return DependencyNodeOperations.getGroupId(n);
   }

   pointcut getArtifactId(DependencyNode n): target(n) && args() && execution(String getArtifactId());

   String around(DependencyNode n) : getArtifactId(n){
      return DependencyNodeOperations.getArtifactId(n);
   }

   pointcut getClassifier(DependencyNode n): target(n) && args() && execution(String getClassifier());

   String around(DependencyNode n) : getClassifier(n){
      return DependencyNodeOperations.getClassifier(n);
   }

   pointcut getType(DependencyNode n): target(n) && args() && execution(String getType());

   String around(DependencyNode n) : getType(n){
      return DependencyNodeOperations.getType(n);
   }

   pointcut getEffectiveVersionConstraint(DependencyNode n): target(n) && args() && execution(String getEffectiveVersionConstraint());

   String around(DependencyNode n) : getEffectiveVersionConstraint(n){
      return DependencyNodeOperations.getEffectiveVersionConstraint(n);
   }

   pointcut getEffectiveScope(DependencyNode n): target(n) && args() && execution(Scope getEffectiveScope());

   Scope around(DependencyNode n) : getEffectiveScope(n){
      return DependencyNodeOperations.getEffectiveScope(n);
   }
}
