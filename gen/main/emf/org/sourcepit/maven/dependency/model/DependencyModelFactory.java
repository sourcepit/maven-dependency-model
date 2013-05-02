/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * 
 * @see org.sourcepit.maven.dependency.model.DependencyModelPackage
 * @generated
 */
public interface DependencyModelFactory extends EFactory
{
   /**
    * The singleton instance of the factory.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   DependencyModelFactory eINSTANCE = org.sourcepit.maven.dependency.model.impl.DependencyModelFactoryImpl.init();

   /**
    * Returns a new object of class '<em>Dependency Node</em>'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return a new object of class '<em>Dependency Node</em>'.
    * @generated
    */
   DependencyNode createDependencyNode();

   /**
    * Returns a new object of class '<em>Dependency Model</em>'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return a new object of class '<em>Dependency Model</em>'.
    * @generated
    */
   DependencyModel createDependencyModel();

   /**
    * Returns a new object of class '<em>Dependency Tree</em>'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return a new object of class '<em>Dependency Tree</em>'.
    * @generated
    */
   DependencyTree createDependencyTree();

   /**
    * Returns the package supported by this factory.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the package supported by this factory.
    * @generated
    */
   DependencyModelPackage getDependencyModelPackage();

} // DependencyModelFactory
