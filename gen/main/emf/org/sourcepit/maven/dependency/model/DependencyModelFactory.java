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
