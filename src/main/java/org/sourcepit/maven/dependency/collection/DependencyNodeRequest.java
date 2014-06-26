/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;


public class DependencyNodeRequest
{
   private final RepositorySystemSession session;

   private final RequestTrace trace;

   private final DependencyNodeManager nodeManager;

   private final DependencyResolutionNode node;

   public DependencyNodeRequest(RepositorySystemSession session, RequestTrace trace, DependencyNodeManager nodeManager,
      DependencyResolutionNode node)
   {
      this.session = session;
      this.trace = trace;
      this.nodeManager = nodeManager;
      this.node = node;
   }

   public RepositorySystemSession getSession()
   {
      return session;
   }

   public RequestTrace getTrace()
   {
      return trace;
   }

   public DependencyNodeManager getNodeManager()
   {
      return nodeManager;
   }

   public DependencyResolutionNode getNode()
   {
      return node;
   }
}
