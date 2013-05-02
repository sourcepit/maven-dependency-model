/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sonatype.aether.graph.DependencyNode;
import org.sourcepit.common.maven.model.VersionConflictKey;

public interface DependencyNode2
{
   DependencyNode getTarget();

   Set<DependencyNode> getParents();

   int getMinimalDepth();

   DependencyNode getReplacement();

   Set<DependencyNode> getReplaced();

   void setReplacement(DependencyNode replacement);

   Set<VersionConflictKey> getConflictKeys();

   VersionConflictKey getDependencyConflictKey();

   VersionConflictKey getArtifactConflictKey();

   Collection<List<DependencyNode>> getConflictingNodeGroups();

   void setVisible(boolean visible);

   boolean isVisible();
}
