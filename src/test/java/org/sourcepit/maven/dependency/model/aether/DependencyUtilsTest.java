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
