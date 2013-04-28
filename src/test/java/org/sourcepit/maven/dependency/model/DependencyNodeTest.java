/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sourcepit.common.maven.model.Scope;

public class DependencyNodeTest
{

   @Test
   public void testGetEffectiveScope()
   {
      DependencyModelFactory eFactory = DependencyModelFactory.eINSTANCE;
      DependencyNode node = eFactory.createDependencyNode();

      try
      {
         node.getEffectiveScope();
         fail();
      }
      catch (NullPointerException e)
      {
      }

      node.setDependencyDeclaration(eFactory.createDeclaredDependency());

      assertNull(node.getManagedScope());
      assertNull(node.getInheritedScope());
      assertSame(Scope.COMPILE, node.getDependencyDeclaration().getScope());
      assertSame(Scope.COMPILE, node.getEffectiveScope());

      node.setInheritedScope(Scope.RUNTIME);

      assertNull(node.getManagedScope());
      assertSame(Scope.RUNTIME, node.getInheritedScope());
      assertSame(Scope.COMPILE, node.getDependencyDeclaration().getScope());
      assertSame(Scope.RUNTIME, node.getEffectiveScope());

      node.setManagedScope(Scope.TEST);

      assertSame(Scope.TEST, node.getManagedScope());
      assertSame(Scope.RUNTIME, node.getInheritedScope());
      assertSame(Scope.COMPILE, node.getDependencyDeclaration().getScope());
      assertSame(Scope.TEST, node.getEffectiveScope());
   }

}
