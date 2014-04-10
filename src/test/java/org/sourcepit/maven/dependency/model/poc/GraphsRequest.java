/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;
public class GraphsRequest extends GraphRequest
{
   private ConflictResolutionScope conflictResolutionScope;

   public ConflictResolutionScope getConflictResolutionScope()
   {
      return conflictResolutionScope;
   }

   public void setConflictResolutionScope(ConflictResolutionScope conflictResolutionScope)
   {
      this.conflictResolutionScope = conflictResolutionScope;
   }

}