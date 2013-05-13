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
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.maven.model.MavenDependency;
import org.sourcepit.common.maven.model.Scope;
import org.sourcepit.maven.dependency.model.DependencyModelPackage;
import org.sourcepit.maven.dependency.model.DependencyNode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Dependency Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getArtifact <em>Artifact</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getChildren <em>Children</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#isSelected <em>Selected</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getParent <em>Parent</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getInheritedScope <em>Inherited Scope</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getManagedVersionConstraint <em>Managed
 * Version Constraint</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getManagedScope <em>Managed Scope</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getConflictNode <em>Conflict Node</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getDeclaredDependency <em>Declared Dependency
 * </em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#isOptional <em>Optional</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl#getConflictVersionConstraint <em>Conflict
 * Version Constraint</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class DependencyNodeImpl extends EObjectImpl implements DependencyNode
{
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
    * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getChildren()
    * @generated
    * @ordered
    */
   protected EList<DependencyNode> children;

   /**
    * The default value of the '{@link #isSelected() <em>Selected</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #isSelected()
    * @generated
    * @ordered
    */
   protected static final boolean SELECTED_EDEFAULT = true;

   /**
    * The cached value of the '{@link #isSelected() <em>Selected</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #isSelected()
    * @generated
    * @ordered
    */
   protected boolean selected = SELECTED_EDEFAULT;

   /**
    * The default value of the '{@link #getInheritedScope() <em>Inherited Scope</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getInheritedScope()
    * @generated
    * @ordered
    */
   protected static final Scope INHERITED_SCOPE_EDEFAULT = null;

   /**
    * The cached value of the '{@link #getInheritedScope() <em>Inherited Scope</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getInheritedScope()
    * @generated
    * @ordered
    */
   protected Scope inheritedScope = INHERITED_SCOPE_EDEFAULT;

   /**
    * The default value of the '{@link #getManagedVersionConstraint() <em>Managed Version Constraint</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getManagedVersionConstraint()
    * @generated
    * @ordered
    */
   protected static final String MANAGED_VERSION_CONSTRAINT_EDEFAULT = null;

   /**
    * The cached value of the '{@link #getManagedVersionConstraint() <em>Managed Version Constraint</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getManagedVersionConstraint()
    * @generated
    * @ordered
    */
   protected String managedVersionConstraint = MANAGED_VERSION_CONSTRAINT_EDEFAULT;

   /**
    * The default value of the '{@link #getManagedScope() <em>Managed Scope</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getManagedScope()
    * @generated
    * @ordered
    */
   protected static final Scope MANAGED_SCOPE_EDEFAULT = null;

   /**
    * The cached value of the '{@link #getManagedScope() <em>Managed Scope</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getManagedScope()
    * @generated
    * @ordered
    */
   protected Scope managedScope = MANAGED_SCOPE_EDEFAULT;

   /**
    * The cached value of the '{@link #getConflictNode() <em>Conflict Node</em>}' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getConflictNode()
    * @generated
    * @ordered
    */
   protected DependencyNode conflictNode;

   /**
    * The cached value of the '{@link #getDeclaredDependency() <em>Declared Dependency</em>}' containment reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getDeclaredDependency()
    * @generated
    * @ordered
    */
   protected MavenDependency declaredDependency;

   /**
    * The default value of the '{@link #isOptional() <em>Optional</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #isOptional()
    * @generated
    * @ordered
    */
   protected static final boolean OPTIONAL_EDEFAULT = false;

   /**
    * The cached value of the '{@link #isOptional() <em>Optional</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #isOptional()
    * @generated
    * @ordered
    */
   protected boolean optional = OPTIONAL_EDEFAULT;

   /**
    * The default value of the '{@link #getConflictVersionConstraint() <em>Conflict Version Constraint</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getConflictVersionConstraint()
    * @generated
    * @ordered
    */
   protected static final String CONFLICT_VERSION_CONSTRAINT_EDEFAULT = null;

   /**
    * The cached value of the '{@link #getConflictVersionConstraint() <em>Conflict Version Constraint</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see #getConflictVersionConstraint()
    * @generated
    * @ordered
    */
   protected String conflictVersionConstraint = CONFLICT_VERSION_CONSTRAINT_EDEFAULT;

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   protected DependencyNodeImpl()
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
      return DependencyModelPackage.Literals.DEPENDENCY_NODE;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public MavenArtifact getArtifact()
   {
      if (artifact != null && artifact.eIsProxy())
      {
         InternalEObject oldArtifact = (InternalEObject) artifact;
         artifact = (MavenArtifact) eResolveProxy(oldArtifact);
         if (artifact != oldArtifact)
         {
            if (eNotificationRequired())
               eNotify(new ENotificationImpl(this, Notification.RESOLVE,
                  DependencyModelPackage.DEPENDENCY_NODE__ARTIFACT, oldArtifact, artifact));
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
   public MavenArtifact basicGetArtifact()
   {
      return artifact;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setArtifact(MavenArtifact newArtifact)
   {
      MavenArtifact oldArtifact = artifact;
      artifact = newArtifact;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_NODE__ARTIFACT,
            oldArtifact, artifact));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EList<DependencyNode> getChildren()
   {
      if (children == null)
      {
         children = new EObjectContainmentWithInverseEList<DependencyNode>(DependencyNode.class, this,
            DependencyModelPackage.DEPENDENCY_NODE__CHILDREN, DependencyModelPackage.DEPENDENCY_NODE__PARENT);
      }
      return children;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public boolean isSelected()
   {
      return selected;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setSelected(boolean newSelected)
   {
      boolean oldSelected = selected;
      selected = newSelected;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_NODE__SELECTED,
            oldSelected, selected));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyNode getParent()
   {
      if (eContainerFeatureID() != DependencyModelPackage.DEPENDENCY_NODE__PARENT)
         return null;
      return (DependencyNode) eInternalContainer();
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public NotificationChain basicSetParent(DependencyNode newParent, NotificationChain msgs)
   {
      msgs = eBasicSetContainer((InternalEObject) newParent, DependencyModelPackage.DEPENDENCY_NODE__PARENT, msgs);
      return msgs;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setParent(DependencyNode newParent)
   {
      if (newParent != eInternalContainer()
         || (eContainerFeatureID() != DependencyModelPackage.DEPENDENCY_NODE__PARENT && newParent != null))
      {
         if (EcoreUtil.isAncestor(this, newParent))
            throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
         NotificationChain msgs = null;
         if (eInternalContainer() != null)
            msgs = eBasicRemoveFromContainer(msgs);
         if (newParent != null)
            msgs = ((InternalEObject) newParent).eInverseAdd(this, DependencyModelPackage.DEPENDENCY_NODE__CHILDREN,
               DependencyNode.class, msgs);
         msgs = basicSetParent(newParent, msgs);
         if (msgs != null)
            msgs.dispatch();
      }
      else if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_NODE__PARENT,
            newParent, newParent));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public Scope getInheritedScope()
   {
      return inheritedScope;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setInheritedScope(Scope newInheritedScope)
   {
      Scope oldInheritedScope = inheritedScope;
      inheritedScope = newInheritedScope;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_NODE__INHERITED_SCOPE,
            oldInheritedScope, inheritedScope));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public String getManagedVersionConstraint()
   {
      return managedVersionConstraint;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setManagedVersionConstraint(String newManagedVersionConstraint)
   {
      String oldManagedVersionConstraint = managedVersionConstraint;
      managedVersionConstraint = newManagedVersionConstraint;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET,
            DependencyModelPackage.DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT, oldManagedVersionConstraint,
            managedVersionConstraint));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public Scope getManagedScope()
   {
      return managedScope;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setManagedScope(Scope newManagedScope)
   {
      Scope oldManagedScope = managedScope;
      managedScope = newManagedScope;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_NODE__MANAGED_SCOPE,
            oldManagedScope, managedScope));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyNode getConflictNode()
   {
      if (conflictNode != null && conflictNode.eIsProxy())
      {
         InternalEObject oldConflictNode = (InternalEObject) conflictNode;
         conflictNode = (DependencyNode) eResolveProxy(oldConflictNode);
         if (conflictNode != oldConflictNode)
         {
            if (eNotificationRequired())
               eNotify(new ENotificationImpl(this, Notification.RESOLVE,
                  DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_NODE, oldConflictNode, conflictNode));
         }
      }
      return conflictNode;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyNode basicGetConflictNode()
   {
      return conflictNode;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setConflictNode(DependencyNode newConflictNode)
   {
      DependencyNode oldConflictNode = conflictNode;
      conflictNode = newConflictNode;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_NODE,
            oldConflictNode, conflictNode));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public MavenDependency getDeclaredDependency()
   {
      return declaredDependency;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public NotificationChain basicSetDeclaredDependency(MavenDependency newDeclaredDependency, NotificationChain msgs)
   {
      MavenDependency oldDeclaredDependency = declaredDependency;
      declaredDependency = newDeclaredDependency;
      if (eNotificationRequired())
      {
         ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
            DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY, oldDeclaredDependency, newDeclaredDependency);
         if (msgs == null)
            msgs = notification;
         else
            msgs.add(notification);
      }
      return msgs;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setDeclaredDependency(MavenDependency newDeclaredDependency)
   {
      if (newDeclaredDependency != declaredDependency)
      {
         NotificationChain msgs = null;
         if (declaredDependency != null)
            msgs = ((InternalEObject) declaredDependency).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
               - DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY, null, msgs);
         if (newDeclaredDependency != null)
            msgs = ((InternalEObject) newDeclaredDependency).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
               - DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY, null, msgs);
         msgs = basicSetDeclaredDependency(newDeclaredDependency, msgs);
         if (msgs != null)
            msgs.dispatch();
      }
      else if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET,
            DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY, newDeclaredDependency, newDeclaredDependency));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public boolean isOptional()
   {
      return optional;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setOptional(boolean newOptional)
   {
      boolean oldOptional = optional;
      optional = newOptional;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, DependencyModelPackage.DEPENDENCY_NODE__OPTIONAL,
            oldOptional, optional));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public String getConflictVersionConstraint()
   {
      return conflictVersionConstraint;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void setConflictVersionConstraint(String newConflictVersionConstraint)
   {
      String oldConflictVersionConstraint = conflictVersionConstraint;
      conflictVersionConstraint = newConflictVersionConstraint;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET,
            DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_VERSION_CONSTRAINT, oldConflictVersionConstraint,
            conflictVersionConstraint));
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public String getGroupId()
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
   public String getArtifactId()
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
   public Scope getEffectiveScope()
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
   public String getEffectiveVersionConstraint()
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
   public String getClassifier()
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
   public String getType()
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
   @SuppressWarnings("unchecked")
   @Override
   public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs)
   {
      switch (featureID)
      {
         case DependencyModelPackage.DEPENDENCY_NODE__CHILDREN :
            return ((InternalEList<InternalEObject>) (InternalEList<?>) getChildren()).basicAdd(otherEnd, msgs);
         case DependencyModelPackage.DEPENDENCY_NODE__PARENT :
            if (eInternalContainer() != null)
               msgs = eBasicRemoveFromContainer(msgs);
            return basicSetParent((DependencyNode) otherEnd, msgs);
      }
      return super.eInverseAdd(otherEnd, featureID, msgs);
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
         case DependencyModelPackage.DEPENDENCY_NODE__CHILDREN :
            return ((InternalEList<?>) getChildren()).basicRemove(otherEnd, msgs);
         case DependencyModelPackage.DEPENDENCY_NODE__PARENT :
            return basicSetParent(null, msgs);
         case DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY :
            return basicSetDeclaredDependency(null, msgs);
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
   public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs)
   {
      switch (eContainerFeatureID())
      {
         case DependencyModelPackage.DEPENDENCY_NODE__PARENT :
            return eInternalContainer().eInverseRemove(this, DependencyModelPackage.DEPENDENCY_NODE__CHILDREN,
               DependencyNode.class, msgs);
      }
      return super.eBasicRemoveFromContainerFeature(msgs);
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
         case DependencyModelPackage.DEPENDENCY_NODE__ARTIFACT :
            if (resolve)
               return getArtifact();
            return basicGetArtifact();
         case DependencyModelPackage.DEPENDENCY_NODE__CHILDREN :
            return getChildren();
         case DependencyModelPackage.DEPENDENCY_NODE__SELECTED :
            return isSelected();
         case DependencyModelPackage.DEPENDENCY_NODE__PARENT :
            return getParent();
         case DependencyModelPackage.DEPENDENCY_NODE__INHERITED_SCOPE :
            return getInheritedScope();
         case DependencyModelPackage.DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT :
            return getManagedVersionConstraint();
         case DependencyModelPackage.DEPENDENCY_NODE__MANAGED_SCOPE :
            return getManagedScope();
         case DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_NODE :
            if (resolve)
               return getConflictNode();
            return basicGetConflictNode();
         case DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY :
            return getDeclaredDependency();
         case DependencyModelPackage.DEPENDENCY_NODE__OPTIONAL :
            return isOptional();
         case DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_VERSION_CONSTRAINT :
            return getConflictVersionConstraint();
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
         case DependencyModelPackage.DEPENDENCY_NODE__ARTIFACT :
            setArtifact((MavenArtifact) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__CHILDREN :
            getChildren().clear();
            getChildren().addAll((Collection<? extends DependencyNode>) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__SELECTED :
            setSelected((Boolean) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__PARENT :
            setParent((DependencyNode) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__INHERITED_SCOPE :
            setInheritedScope((Scope) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT :
            setManagedVersionConstraint((String) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__MANAGED_SCOPE :
            setManagedScope((Scope) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_NODE :
            setConflictNode((DependencyNode) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY :
            setDeclaredDependency((MavenDependency) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__OPTIONAL :
            setOptional((Boolean) newValue);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_VERSION_CONSTRAINT :
            setConflictVersionConstraint((String) newValue);
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
         case DependencyModelPackage.DEPENDENCY_NODE__ARTIFACT :
            setArtifact((MavenArtifact) null);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__CHILDREN :
            getChildren().clear();
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__SELECTED :
            setSelected(SELECTED_EDEFAULT);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__PARENT :
            setParent((DependencyNode) null);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__INHERITED_SCOPE :
            setInheritedScope(INHERITED_SCOPE_EDEFAULT);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT :
            setManagedVersionConstraint(MANAGED_VERSION_CONSTRAINT_EDEFAULT);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__MANAGED_SCOPE :
            setManagedScope(MANAGED_SCOPE_EDEFAULT);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_NODE :
            setConflictNode((DependencyNode) null);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY :
            setDeclaredDependency((MavenDependency) null);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__OPTIONAL :
            setOptional(OPTIONAL_EDEFAULT);
            return;
         case DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_VERSION_CONSTRAINT :
            setConflictVersionConstraint(CONFLICT_VERSION_CONSTRAINT_EDEFAULT);
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
         case DependencyModelPackage.DEPENDENCY_NODE__ARTIFACT :
            return artifact != null;
         case DependencyModelPackage.DEPENDENCY_NODE__CHILDREN :
            return children != null && !children.isEmpty();
         case DependencyModelPackage.DEPENDENCY_NODE__SELECTED :
            return selected != SELECTED_EDEFAULT;
         case DependencyModelPackage.DEPENDENCY_NODE__PARENT :
            return getParent() != null;
         case DependencyModelPackage.DEPENDENCY_NODE__INHERITED_SCOPE :
            return INHERITED_SCOPE_EDEFAULT == null ? inheritedScope != null : !INHERITED_SCOPE_EDEFAULT
               .equals(inheritedScope);
         case DependencyModelPackage.DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT :
            return MANAGED_VERSION_CONSTRAINT_EDEFAULT == null
               ? managedVersionConstraint != null
               : !MANAGED_VERSION_CONSTRAINT_EDEFAULT.equals(managedVersionConstraint);
         case DependencyModelPackage.DEPENDENCY_NODE__MANAGED_SCOPE :
            return MANAGED_SCOPE_EDEFAULT == null ? managedScope != null : !MANAGED_SCOPE_EDEFAULT.equals(managedScope);
         case DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_NODE :
            return conflictNode != null;
         case DependencyModelPackage.DEPENDENCY_NODE__DECLARED_DEPENDENCY :
            return declaredDependency != null;
         case DependencyModelPackage.DEPENDENCY_NODE__OPTIONAL :
            return optional != OPTIONAL_EDEFAULT;
         case DependencyModelPackage.DEPENDENCY_NODE__CONFLICT_VERSION_CONSTRAINT :
            return CONFLICT_VERSION_CONSTRAINT_EDEFAULT == null
               ? conflictVersionConstraint != null
               : !CONFLICT_VERSION_CONSTRAINT_EDEFAULT.equals(conflictVersionConstraint);
      }
      return super.eIsSet(featureID);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String toString()
   {
      if (eIsProxy())
         return super.toString();

      StringBuffer result = new StringBuffer(super.toString());
      result.append(" (selected: ");
      result.append(selected);
      result.append(", inheritedScope: ");
      result.append(inheritedScope);
      result.append(", managedVersionConstraint: ");
      result.append(managedVersionConstraint);
      result.append(", managedScope: ");
      result.append(managedScope);
      result.append(", optional: ");
      result.append(optional);
      result.append(", conflictVersionConstraint: ");
      result.append(conflictVersionConstraint);
      result.append(')');
      return result.toString();
   }

} // DependencyNodeImpl
