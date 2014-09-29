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

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyModelPackage;
import org.sourcepit.maven.dependency.model.DependencyTree;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Dependency Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyModelImpl#getArtifacts <em>Artifacts</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyModelImpl#getDependencyTrees <em>Dependency Trees
 * </em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyModelImpl#getRootArtifacts <em>Root Artifacts</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class DependencyModelImpl extends EObjectImpl implements DependencyModel
{
   /**
    * The cached value of the '{@link #getArtifacts() <em>Artifacts</em>}' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getArtifacts()
    * @generated
    * @ordered
    */
   protected EList<MavenArtifact> artifacts;

   /**
    * The cached value of the '{@link #getDependencyTrees() <em>Dependency Trees</em>}' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getDependencyTrees()
    * @generated
    * @ordered
    */
   protected EList<DependencyTree> dependencyTrees;

   /**
    * The cached value of the '{@link #getRootArtifacts() <em>Root Artifacts</em>}' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getRootArtifacts()
    * @generated
    * @ordered
    */
   protected EList<MavenArtifact> rootArtifacts;

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   protected DependencyModelImpl()
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
      return DependencyModelPackage.Literals.DEPENDENCY_MODEL;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EList<MavenArtifact> getArtifacts()
   {
      if (artifacts == null)
      {
         artifacts = new EObjectContainmentEList<MavenArtifact>(MavenArtifact.class, this,
            DependencyModelPackage.DEPENDENCY_MODEL__ARTIFACTS);
      }
      return artifacts;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EList<DependencyTree> getDependencyTrees()
   {
      if (dependencyTrees == null)
      {
         dependencyTrees = new EObjectContainmentEList<DependencyTree>(DependencyTree.class, this,
            DependencyModelPackage.DEPENDENCY_MODEL__DEPENDENCY_TREES);
      }
      return dependencyTrees;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EList<MavenArtifact> getRootArtifacts()
   {
      if (rootArtifacts == null)
      {
         rootArtifacts = new EObjectResolvingEList<MavenArtifact>(MavenArtifact.class, this,
            DependencyModelPackage.DEPENDENCY_MODEL__ROOT_ARTIFACTS);
      }
      return rootArtifacts;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyTree getDependencyTree(MavenArtifact artifact)
   {
      // TODO: implement this method
      // Ensure that you remove @generated or mark it @generated NOT
      throw new UnsupportedOperationException();
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyTree getDependencyTree(ArtifactKey artifactKey)
   {
      // TODO: implement this method
      // Ensure that you remove @generated or mark it @generated NOT
      throw new UnsupportedOperationException();
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public MavenArtifact getArtifact(ArtifactKey artifactKey)
   {
      // TODO: implement this method
      // Ensure that you remove @generated or mark it @generated NOT
      throw new UnsupportedOperationException();
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_MODEL__ARTIFACTS :
            return ((InternalEList<?>) getArtifacts()).basicRemove(otherEnd, msgs);
         case DependencyModelPackage.DEPENDENCY_MODEL__DEPENDENCY_TREES :
            return ((InternalEList<?>) getDependencyTrees()).basicRemove(otherEnd, msgs);
      }
      return super.eInverseRemove(otherEnd, featureID, msgs);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Object eGet(int featureID, boolean resolve, boolean coreType)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_MODEL__ARTIFACTS :
            return getArtifacts();
         case DependencyModelPackage.DEPENDENCY_MODEL__DEPENDENCY_TREES :
            return getDependencyTrees();
         case DependencyModelPackage.DEPENDENCY_MODEL__ROOT_ARTIFACTS :
            return getRootArtifacts();
      }
      return super.eGet(featureID, resolve, coreType);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @SuppressWarnings("unchecked")
   @Override
   public void eSet(int featureID, Object newValue)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_MODEL__ARTIFACTS :
            getArtifacts().clear();
            getArtifacts().addAll((Collection<? extends MavenArtifact>) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_MODEL__DEPENDENCY_TREES :
            getDependencyTrees().clear();
            getDependencyTrees().addAll((Collection<? extends DependencyTree>) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_MODEL__ROOT_ARTIFACTS :
            getRootArtifacts().clear();
            getRootArtifacts().addAll((Collection<? extends MavenArtifact>) newValue);
            return;
      }
      super.eSet(featureID, newValue);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public void eUnset(int featureID)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_MODEL__ARTIFACTS :
            getArtifacts().clear();
            return;
         case DependencyModelPackage.DEPENDENCY_MODEL__DEPENDENCY_TREES :
            getDependencyTrees().clear();
            return;
         case DependencyModelPackage.DEPENDENCY_MODEL__ROOT_ARTIFACTS :
            getRootArtifacts().clear();
            return;
      }
      super.eUnset(featureID);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public boolean eIsSet(int featureID)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_MODEL__ARTIFACTS :
            return artifacts != null && !artifacts.isEmpty();
         case DependencyModelPackage.DEPENDENCY_MODEL__DEPENDENCY_TREES :
            return dependencyTrees != null && !dependencyTrees.isEmpty();
         case DependencyModelPackage.DEPENDENCY_MODEL__ROOT_ARTIFACTS :
            return rootArtifacts != null && !rootArtifacts.isEmpty();
      }
      return super.eIsSet(featureID);
   }

} // DependencyModelImpl
