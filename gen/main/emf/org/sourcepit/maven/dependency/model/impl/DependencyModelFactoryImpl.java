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

package org.sourcepit.maven.dependency.model.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyModelFactory;
import org.sourcepit.maven.dependency.model.DependencyModelPackage;
import org.sourcepit.maven.dependency.model.DependencyNode;
import org.sourcepit.maven.dependency.model.DependencyTree;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * 
 * @generated
 */
public class DependencyModelFactoryImpl extends EFactoryImpl implements DependencyModelFactory
{
   /**
    * Creates the default factory implementation.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public static DependencyModelFactory init()
   {
      try
      {
         DependencyModelFactory theDependencyModelFactory = (DependencyModelFactory) EPackage.Registry.INSTANCE
            .getEFactory(DependencyModelPackage.eNS_URI);
         if (theDependencyModelFactory != null)
         {
            return theDependencyModelFactory;
         }
      }
      catch (Exception exception)
      {
         EcorePlugin.INSTANCE.log(exception);
      }
      return new DependencyModelFactoryImpl();
   }

   /**
    * Creates an instance of the factory.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyModelFactoryImpl()
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
   public EObject create(EClass eClass)
   {
      switch (eClass.getClassifierID())
      {
         case DependencyModelPackage.DEPENDENCY_NODE :
            return createDependencyNode();
         case DependencyModelPackage.DEPENDENCY_MODEL :
            return createDependencyModel();
         case DependencyModelPackage.DEPENDENCY_TREE :
            return createDependencyTree();
         default :
            throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
      }
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyNode createDependencyNode()
   {
      DependencyNodeImpl dependencyNode = new DependencyNodeImpl();
      return dependencyNode;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyModel createDependencyModel()
   {
      DependencyModelImpl dependencyModel = new DependencyModelImpl();
      return dependencyModel;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyTree createDependencyTree()
   {
      DependencyTreeImpl dependencyTree = new DependencyTreeImpl();
      return dependencyTree;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyModelPackage getDependencyModelPackage()
   {
      return (DependencyModelPackage) getEPackage();
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @deprecated
    * @generated
    */
   @Deprecated
   public static DependencyModelPackage getPackage()
   {
      return DependencyModelPackage.eINSTANCE;
   }

} // DependencyModelFactoryImpl
