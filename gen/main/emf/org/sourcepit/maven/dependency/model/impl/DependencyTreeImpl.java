/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyTreeImpl#getTargetArtifact <em>Target Artifact</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyTreeImpl#getDependencyNodes <em>Dependency Nodes</em>}
 * </li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class DependencyTreeImpl extends EObjectImpl implements DependencyTree
{
   /**
    * The cached value of the '{@link #getTargetArtifact() <em>Target Artifact</em>}' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getTargetArtifact()
    * @generated
    * @ordered
    */
   protected MavenArtifact targetArtifact;

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
   protected DependencyTreeImpl()
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
      return DependencyModelPackage.Literals.DEPENDENCY_TREE;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public MavenArtifact getTargetArtifact()
   {
      if (targetArtifact != null && targetArtifact.eIsProxy())
      {
         InternalEObject oldTargetArtifact = (InternalEObject) targetArtifact;
         targetArtifact = (MavenArtifact) eResolveProxy(oldTargetArtifact);
         if (targetArtifact != oldTargetArtifact)
         {
            if (eNotificationRequired())
               eNotify(new ENotificationImpl(this, Notification.RESOLVE,
                  DependencyModelPackage.DEPENDENCY_TREE__TARGET_ARTIFACT, oldTargetArtifact, targetArtifact));
         }
      }
      return targetArtifact;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public MavenArtifact basicGetTargetArtifact()
   {
      return targetArtifact;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setTargetArtifact(MavenArtifact newTargetArtifact)
   {
      MavenArtifact oldTargetArtifact = targetArtifact;
      targetArtifact = newTargetArtifact;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_TREE__TARGET_ARTIFACT,
            oldTargetArtifact, targetArtifact));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EList<DependencyNode> getDependencyNodes()
   {
      if (dependencyNodes == null)
      {
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
   public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
   {
      switch (featureID)
      {
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
   public Object eGet(int featureID, boolean resolve, boolean coreType)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_TREE__TARGET_ARTIFACT :
            if (resolve)
               return getTargetArtifact();
            return basicGetTargetArtifact();
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
   public void eSet(int featureID, Object newValue)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_TREE__TARGET_ARTIFACT :
            setTargetArtifact((MavenArtifact) newValue);
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
   public void eUnset(int featureID)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_TREE__TARGET_ARTIFACT :
            setTargetArtifact((MavenArtifact) null);
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
   public boolean eIsSet(int featureID)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_TREE__TARGET_ARTIFACT :
            return targetArtifact != null;
         case DependencyModelPackage.DEPENDENCY_TREE__DEPENDENCY_NODES :
            return dependencyNodes != null && !dependencyNodes.isEmpty();
      }
      return super.eIsSet(featureID);
   }

} // DependencyTreeImpl
