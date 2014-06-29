/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;


public class DependencyNodeRequest
{
   private final RepositorySystemSession session;

   private final String requestContext;

   private final RequestTrace trace;

   private final DependencyNodeManager nodeManager;

   private final DependencyNode node;

   public DependencyNodeRequest(RepositorySystemSession session, RequestTrace trace, String requestContext,
      DependencyNodeManager nodeManager, DependencyNode node)
   {
      this.session = session;
      this.trace = trace;
      this.requestContext = requestContext;
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

   public String getRequestContext()
   {
      return requestContext;
   }

   public DependencyNodeManager getNodeManager()
   {
      return nodeManager;
   }

   public DependencyNode getNode()
   {
      return node;
   }
}
