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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.maven.dependency.model.DependencyModelPackage;
import org.sourcepit.maven.dependency.model.DependencyNode;
import org.sourcepit.maven.dependency.model.DependencyTree;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Dependency Tree</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyTreeImpl#getArtifact <em>Artifact</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyTreeImpl#getDependencyNodes <em>Dependency Nodes</em>}
 * </li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class DependencyTreeImpl extends EObjectImpl implements DependencyTree {
   /**
    * The cached value of the '{@link #getArtifact() <em>Artifact</em>}' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getArtifact()
    * @generated
    * @ordered
    */
   protected MavenArtifact artifact;

   /**
    * The cached value of the '{@link #getDependencyNodes() <em>Dependency Nodes</em>}' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getDependencyNodes()
    * @generated
    * @ordered
    */
   protected EList<DependencyNode> dependencyNodes;

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   protected DependencyTreeImpl() {
      super();
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   protected EClass eStaticClass() {
      return DependencyModelPackage.Literals.DEPENDENCY_TREE;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public MavenArtifact getArtifact() {
      if (artifact != null && artifact.eIsProxy()) {
         InternalEObject oldArtifact = (InternalEObject) artifact;
         artifact = (MavenArtifact) eResolveProxy(oldArtifact);
         if (artifact != oldArtifact) {
            if (eNotificationRequired())
               eNotify(new ENotificationImpl(this, Notification.RESOLVE,
                  DependencyModelPackage.DEPENDENCY_TREE__ARTIFACT, oldArtifact, artifact));
         }
      }
      return artifact;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public MavenArtifact basicGetArtifact() {
      return artifact;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setArtifact(MavenArtifact newArtifact) {
      MavenArtifact oldArtifact = artifact;
      artifact = newArtifact;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_TREE__ARTIFACT,
            oldArtifact, artifact));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EList<DependencyNode> getDependencyNodes() {
      if (dependencyNodes == null) {
         dependencyNodes = new EObjectContainmentEList<DependencyNode>(DependencyNode.class, this,
            DependencyModelPackage.DEPENDENCY_TREE__DEPENDENCY_NODES);
      }
      return dependencyNodes;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
      switch (featureID) {
         case DependencyModelPackage.DEPENDENCY_TREE__DEPENDENCY_NODES :
            return ((InternalEList<?>) getDependencyNodes()).basicRemove(otherEnd, msgs);
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
   public Object eGet(int featureID, boolean resolve, boolean coreType) {
      switch (featureID) {
         case DependencyModelPackage.DEPENDENCY_TREE__ARTIFACT :
            if (resolve)
               return getArtifact();
            return basicGetArtifact();
         case DependencyModelPackage.DEPENDENCY_TREE__DEPENDENCY_NODES :
            return getDependencyNodes();
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
   public void eSet(int featureID, Object newValue) {
      switch (featureID) {
         case DependencyModelPackage.DEPENDENCY_TREE__ARTIFACT :
            setArtifact((MavenArtifact) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_TREE__DEPENDENCY_NODES :
            getDependencyNodes().clear();
            getDependencyNodes().addAll((Collection<? extends DependencyNode>) newValue);
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
   public void eUnset(int featureID) {
      switch (featureID) {
         case DependencyModelPackage.DEPENDENCY_TREE__ARTIFACT :
            setArtifact((MavenArtifact) null);
            return;
         case DependencyModelPackage.DEPENDENCY_TREE__DEPENDENCY_NODES :
            getDependencyNodes().clear();
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
   public boolean eIsSet(int featureID) {
      switch (featureID) {
         case DependencyModelPackage.DEPENDENCY_TREE__ARTIFACT :
            return artifact != null;
         case DependencyModelPackage.DEPENDENCY_TREE__DEPENDENCY_NODES :
            return dependencyNodes != null && !dependencyNodes.isEmpty();
      }
      return super.eIsSet(featureID);
   }

} // DependencyTreeImpl
