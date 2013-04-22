
package org.sourcepit.maven.dependency.model;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.test.util.impl.TestDependencyNode;
import org.sonatype.aether.test.util.impl.TestVersionScheme;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.VersionScheme;

/**
 * @author Benjamin Bentmann
 */
public class NodeBuilder
{

   private String groupId = "test";

   private String artifactId = "";

   private String version = "0.1";

   private String range;

   private String ext = "jar";

   private String classifier = "";

   private String scope = "compile";

   private boolean optional = false;

   private String context;

   private List<Artifact> relocations = new ArrayList<Artifact>();

   private VersionScheme versionScheme = new TestVersionScheme();

   private Map<String, String> properties = new HashMap<String, String>(0);

   public NodeBuilder artifactId(String artifactId)
   {
      this.artifactId = artifactId;
      return this;
   }

   public NodeBuilder groupId(String groupId)
   {
      this.groupId = groupId;
      return this;

   }

   public NodeBuilder ext(String ext)
   {
      this.ext = ext;
      return this;
   }

   public NodeBuilder version(String version)
   {
      this.version = version;
      this.range = null;
      return this;
   }

   public NodeBuilder range(String range)
   {
      this.range = range;
      return this;
   }

   public NodeBuilder scope(String scope)
   {
      this.scope = scope;
      return this;
   }

   public NodeBuilder optional(boolean optional)
   {
      this.optional = optional;
      return this;
   }

   public NodeBuilder context(String context)
   {
      this.context = context;
      return this;
   }

   public NodeBuilder reloc(String artifactId)
   {
      Artifact relocation = new StubArtifact(groupId, artifactId, classifier, ext, version);
      relocations.add(relocation);
      return this;
   }

   public NodeBuilder reloc(String groupId, String artifactId, String version)
   {
      Artifact relocation = new StubArtifact(groupId, artifactId, classifier, ext, version);
      relocations.add(relocation);
      return this;
   }

   public NodeBuilder properties(Map<String, String> properties)
   {
      this.properties = properties != null ? properties : Collections.<String, String> emptyMap();
      return this;
   }

   public DependencyNode build()
   {
      Dependency dependency = null;
      TestDependencyNode node = new TestDependencyNode();
      if (artifactId != null && artifactId.length() > 0)
      {
         Artifact artifact = new StubArtifact(groupId, artifactId, classifier, ext, version, properties);
         dependency = new Dependency(artifact, scope, optional);
         node.setDependency(dependency);
         try
         {
            node.setVersion(versionScheme.parseVersion(version));
            node.setVersionConstraint(versionScheme.parseVersionConstraint(range != null ? range : version));
         }
         catch (InvalidVersionSpecificationException e)
         {
            throw new IllegalArgumentException("bad version: " + e.getMessage(), e);
         }
      }
      node.setRequestContext(context);
      node.setRelocations(relocations);
      return node;
   }

}
