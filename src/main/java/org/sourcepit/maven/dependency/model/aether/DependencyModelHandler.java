/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;

public interface DependencyModelHandler
{
   void artifact(Artifact artifact, boolean referenced);
   
   void startDependencyModel();

   boolean startDependencyTree(Artifact artifact);

   void startDependencyNode(DependencyNode node, String scope, boolean optional, boolean selected,
      DependencyNode shadowedNode);

   void endDependencyNode(DependencyNode node);

   void endDependencyTree(Artifact artifact);

   void endDependencyModel();
}
