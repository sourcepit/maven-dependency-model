/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.aether;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class DependencyUtilsTest
{
   @Test
   public void getEffectiveScope_EraseFalse() throws Exception
   {
      try
      {
         DependencyUtils.getEffectiveScope(null, "compile", false);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope(null, "provided", false);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope(null, "runtime", false);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope(null, "test", false);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope(null, "system", false);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope("system", "compile", false);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      assertEquals("compile", DependencyUtils.getEffectiveScope("compile", "compile", false));
      assertEquals("provided", DependencyUtils.getEffectiveScope("compile", "provided", false));
      assertEquals("runtime", DependencyUtils.getEffectiveScope("compile", "runtime", false));
      assertEquals("test", DependencyUtils.getEffectiveScope("compile", "test", false));
      assertEquals("system", DependencyUtils.getEffectiveScope("compile", "system", false));

      assertEquals("provided", DependencyUtils.getEffectiveScope("provided", "compile", false));
      assertEquals("provided", DependencyUtils.getEffectiveScope("provided", "provided", false));
      assertEquals("provided", DependencyUtils.getEffectiveScope("provided", "runtime", false));
      assertEquals("test", DependencyUtils.getEffectiveScope("provided", "test", false));
      assertEquals("system", DependencyUtils.getEffectiveScope("provided", "system", false));

      assertEquals("runtime", DependencyUtils.getEffectiveScope("runtime", "compile", false));
      assertEquals("provided", DependencyUtils.getEffectiveScope("runtime", "provided", false));
      assertEquals("runtime", DependencyUtils.getEffectiveScope("runtime", "runtime", false));
      assertEquals("test", DependencyUtils.getEffectiveScope("runtime", "test", false));
      assertEquals("system", DependencyUtils.getEffectiveScope("runtime", "system", false));

      assertEquals("test", DependencyUtils.getEffectiveScope("test", "compile", false));
      assertEquals("provided", DependencyUtils.getEffectiveScope("test", "provided", false));
      assertEquals("test", DependencyUtils.getEffectiveScope("test", "runtime", false));
      assertEquals("test", DependencyUtils.getEffectiveScope("test", "test", false));
      assertEquals("system", DependencyUtils.getEffectiveScope("test", "system", false));
   }

   @Test
   public void getEffectiveScope_EraseTrue() throws Exception
   {
      try
      {
         DependencyUtils.getEffectiveScope(null, "compile");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope(null, "provided");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope(null, "runtime");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope(null, "test");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope(null, "system");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DependencyUtils.getEffectiveScope("system", "compile");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      assertEquals("compile", DependencyUtils.getEffectiveScope("compile", "compile"));
      assertEquals(null, DependencyUtils.getEffectiveScope("compile", "provided"));
      assertEquals(null, DependencyUtils.getEffectiveScope("compile", "system"));
      assertEquals("runtime", DependencyUtils.getEffectiveScope("compile", "runtime"));
      assertEquals(null, DependencyUtils.getEffectiveScope("compile", "test"));

      assertEquals("provided", DependencyUtils.getEffectiveScope("provided", "compile"));
      assertEquals(null, DependencyUtils.getEffectiveScope("provided", "provided"));
      assertEquals(null, DependencyUtils.getEffectiveScope("provided", "system"));
      assertEquals("provided", DependencyUtils.getEffectiveScope("provided", "runtime"));
      assertEquals(null, DependencyUtils.getEffectiveScope("provided", "test"));

      assertEquals("runtime", DependencyUtils.getEffectiveScope("runtime", "compile"));
      assertEquals(null, DependencyUtils.getEffectiveScope("runtime", "provided"));
      assertEquals(null, DependencyUtils.getEffectiveScope("runtime", "system"));
      assertEquals("runtime", DependencyUtils.getEffectiveScope("runtime", "runtime"));
      assertEquals(null, DependencyUtils.getEffectiveScope("runtime", "test"));

      assertEquals("test", DependencyUtils.getEffectiveScope("test", "compile"));
      assertEquals(null, DependencyUtils.getEffectiveScope("test", "provided"));
      assertEquals(null, DependencyUtils.getEffectiveScope("test", "system"));
      assertEquals("test", DependencyUtils.getEffectiveScope("test", "runtime"));
      assertEquals(null, DependencyUtils.getEffectiveScope("test", "test"));
   }
}
