/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency;

import org.eclipse.aether.graph.Dependency;

public class ManagedDependency
{
   private Dependency managedDependency;

   private byte managedBits;

   public void setDependency(Dependency managedDependency)
   {
      this.managedDependency = managedDependency;
   }

   public Dependency getDependency()
   {
      return managedDependency;
   }

   public int getManagedBits()
   {
      return managedBits;
   }

   public void setManagedBits(int managedBits)
   {
      this.managedBits = (byte) (managedBits & 0x1F);
   }
}