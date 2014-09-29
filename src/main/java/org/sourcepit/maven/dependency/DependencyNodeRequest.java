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

package org.sourcepit.maven.dependency;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;


public class DependencyNodeRequest
{
   private final RepositorySystemSession session;

   private final RequestTrace trace;

   private final DependencyNodeManager nodeManager;

   private final DependencyNode node;

   public DependencyNodeRequest(RepositorySystemSession session, RequestTrace trace, DependencyNodeManager nodeManager,
      DependencyNode node)
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

   public DependencyNode getNode()
   {
      return node;
   }
}
