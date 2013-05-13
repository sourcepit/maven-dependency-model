/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
