/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.List;

import org.eclipse.aether.version.Version;

public interface ConflictSolver
{
   Version determineResolvedVersion(DependencyResolutionNode request);
   
   DependencyResolutionNode detectCyclicParent(DependencyResolutionNode node);
   
   void solveConflicts(DependencyResolutionNode parent, List<DependencyResolutionNode> children);
}
