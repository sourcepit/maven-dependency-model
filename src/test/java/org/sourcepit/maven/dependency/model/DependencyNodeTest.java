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

package org.sourcepit.maven.dependency.model;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.sourcepit.common.maven.model.MavenModelFactory;
import org.sourcepit.common.maven.model.Scope;

public class DependencyNodeTest
{

   @Test
   public void testGetEffectiveScope()
   {
      DependencyModelFactory eFactory = DependencyModelFactory.eINSTANCE;
      MavenModelFactory mavenModelFactory = MavenModelFactory.eINSTANCE;

      DependencyNode node = eFactory.createDependencyNode();

      try
      {
         node.getEffectiveScope();
         fail();
      }
      catch (NullPointerException e)
      {
      }

      node.setDeclaredDependency(mavenModelFactory.createMavenDependency());

      assertNull(node.getManagedScope());
      assertNull(node.getInheritedScope());
      assertSame(Scope.COMPILE, node.getDeclaredDependency().getScope());
      assertSame(Scope.COMPILE, node.getEffectiveScope());

      node.setInheritedScope(Scope.RUNTIME);

      assertNull(node.getManagedScope());
      assertSame(Scope.RUNTIME, node.getInheritedScope());
      assertSame(Scope.COMPILE, node.getDeclaredDependency().getScope());
      assertSame(Scope.RUNTIME, node.getEffectiveScope());

      node.setManagedScope(Scope.TEST);

      assertSame(Scope.TEST, node.getManagedScope());
      assertSame(Scope.RUNTIME, node.getInheritedScope());
      assertSame(Scope.COMPILE, node.getDeclaredDependency().getScope());
      assertSame(Scope.TEST, node.getEffectiveScope());
   }

}
