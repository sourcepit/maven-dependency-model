/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.maven.dependency.model.DependencyNodeOperations;


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
