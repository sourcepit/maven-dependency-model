/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.impl;

import org.eclipse.emf.ecore.EClass;
import org.sourcepit.common.maven.model.impl.DependencyDeclarationImpl;
import org.sourcepit.maven.dependency.model.DeclaredDependency;
import org.sourcepit.maven.dependency.model.DependencyModelPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Declared Dependency</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 * 
 * @generated
 */
public class DeclaredDependencyImpl extends DependencyDeclarationImpl implements DeclaredDependency
{
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   protected DeclaredDependencyImpl()
   {
      super();
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   protected EClass eStaticClass()
   {
      return DependencyModelPackage.Literals.DECLARED_DEPENDENCY;
   }

} // DeclaredDependencyImpl
