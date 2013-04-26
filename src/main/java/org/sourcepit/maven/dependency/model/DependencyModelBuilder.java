/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.maven.model.util.MavenModelUtils;

public class DependencyModelBuilder implements DependencyModelHandler
{
   private final DependencyModelFactory eFactory = DependencyModelFactory.eINSTANCE;
   private DependencyModel model;

   @Override
   public void startDependencyModel()
   {
      model = eFactory.createDependencyModel();
   }

   private final Map<String, MavenArtifact> keyToArtifact = new HashMap<String, MavenArtifact>();

   @Override
   public void startDependencyTree(Artifact artifact, boolean referenced)
   {
      if (referenced)
      {
         final String key = MavenModelUtils.toArtifactKey(artifact);
         if (!keyToArtifact.containsKey(key))
         {
            final MavenArtifact mavenArtifact = MavenModelUtils.toMavenArtifact(artifact);
            model.getArtifacts().add(mavenArtifact);
            keyToArtifact.put(key, mavenArtifact);
         }
      }
   }

   @Override
   public void startDependencyNode(DependencyNode node, String scope, boolean optional, boolean selected,
      DependencyNode shadowedNode)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void endDependencyNode(DependencyNode node)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void endDependencyTree(Artifact artifact)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void endDependencyModel()
   {
      // TODO Auto-generated method stub

   }

}
