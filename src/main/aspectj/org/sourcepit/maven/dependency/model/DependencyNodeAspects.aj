/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import org.sourcepit.common.maven.model.Scope;


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
   
   pointcut getEffectiveScope(DependencyNode n): target(n) && args() && execution(Scope getEffectiveScope());
   
   Scope around(DependencyNode n) : getEffectiveScope(n){
      return DependencyNodeOperations.getEffectiveScope(n);
   }
}
