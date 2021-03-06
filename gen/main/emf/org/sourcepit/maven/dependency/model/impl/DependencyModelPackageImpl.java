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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.sourcepit.common.maven.model.MavenModelPackage;
import org.sourcepit.maven.dependency.model.DependencyModel;
import org.sourcepit.maven.dependency.model.DependencyModelFactory;
import org.sourcepit.maven.dependency.model.DependencyModelPackage;
import org.sourcepit.maven.dependency.model.DependencyNode;
import org.sourcepit.maven.dependency.model.DependencyTree;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * 
 * @generated
 */
public class DependencyModelPackageImpl extends EPackageImpl implements DependencyModelPackage {
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   private EClass dependencyNodeEClass = null;

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   private EClass dependencyModelEClass = null;

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   private EClass dependencyTreeEClass = null;

   /**
    * Creates an instance of the model <b>Package</b>, registered with {@link org.eclipse.emf.ecore.EPackage.Registry
    * EPackage.Registry} by the package
    * package URI value.
    * <p>
    * Note: the correct way to create the package is via the static factory method {@link #init init()}, which also
    * performs initialization of the package, or returns the registered package, if one already exists. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see org.eclipse.emf.ecore.EPackage.Registry
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#eNS_URI
    * @see #init()
    * @generated
    */
   private DependencyModelPackageImpl() {
      super(eNS_URI, DependencyModelFactory.eINSTANCE);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   private static boolean isInited = false;

   /**
    * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
    * 
    * <p>
    * This method is used to initialize {@link DependencyModelPackage#eINSTANCE} when that field is accessed. Clients
    * should not invoke it directly. Instead, they should simply access that field to obtain the package. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #eNS_URI
    * @see #createPackageContents()
    * @see #initializePackageContents()
    * @generated
    */
   public static DependencyModelPackage init() {
      if (isInited)
         return (DependencyModelPackage) EPackage.Registry.INSTANCE.getEPackage(DependencyModelPackage.eNS_URI);

      // Obtain or create and register package
      DependencyModelPackageImpl theDependencyModelPackage = (DependencyModelPackageImpl) (EPackage.Registry.INSTANCE.get(eNS_URI) instanceof DependencyModelPackageImpl
         ? EPackage.Registry.INSTANCE.get(eNS_URI)
         : new DependencyModelPackageImpl());

      isInited = true;

      // Initialize simple dependencies
      MavenModelPackage.eINSTANCE.eClass();

      // Create package meta-data objects
      theDependencyModelPackage.createPackageContents();

      // Initialize created meta-data
      theDependencyModelPackage.initializePackageContents();

      // Mark meta-data to indicate it can't be changed
      theDependencyModelPackage.freeze();


      // Update the registry and return the package
      EPackage.Registry.INSTANCE.put(DependencyModelPackage.eNS_URI, theDependencyModelPackage);
      return theDependencyModelPackage;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EClass getDependencyNode() {
      return dependencyNodeEClass;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyNode_Artifact() {
      return (EReference) dependencyNodeEClass.getEStructuralFeatures().get(0);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyNode_Children() {
      return (EReference) dependencyNodeEClass.getEStructuralFeatures().get(1);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EAttribute getDependencyNode_Selected() {
      return (EAttribute) dependencyNodeEClass.getEStructuralFeatures().get(2);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyNode_Parent() {
      return (EReference) dependencyNodeEClass.getEStructuralFeatures().get(3);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EAttribute getDependencyNode_InheritedScope() {
      return (EAttribute) dependencyNodeEClass.getEStructuralFeatures().get(4);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EAttribute getDependencyNode_ManagedVersionConstraint() {
      return (EAttribute) dependencyNodeEClass.getEStructuralFeatures().get(5);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EAttribute getDependencyNode_ManagedScope() {
      return (EAttribute) dependencyNodeEClass.getEStructuralFeatures().get(6);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyNode_ConflictNode() {
      return (EReference) dependencyNodeEClass.getEStructuralFeatures().get(7);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyNode_DeclaredDependency() {
      return (EReference) dependencyNodeEClass.getEStructuralFeatures().get(8);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EAttribute getDependencyNode_Optional() {
      return (EAttribute) dependencyNodeEClass.getEStructuralFeatures().get(9);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EAttribute getDependencyNode_ConflictVersionConstraint() {
      return (EAttribute) dependencyNodeEClass.getEStructuralFeatures().get(10);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyNode_CycleNode() {
      return (EReference) dependencyNodeEClass.getEStructuralFeatures().get(11);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EClass getDependencyModel() {
      return dependencyModelEClass;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyModel_Artifacts() {
      return (EReference) dependencyModelEClass.getEStructuralFeatures().get(0);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyModel_DependencyTrees() {
      return (EReference) dependencyModelEClass.getEStructuralFeatures().get(1);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyModel_RootArtifacts() {
      return (EReference) dependencyModelEClass.getEStructuralFeatures().get(2);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EClass getDependencyTree() {
      return dependencyTreeEClass;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyTree_Artifact() {
      return (EReference) dependencyTreeEClass.getEStructuralFeatures().get(0);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public EReference getDependencyTree_DependencyNodes() {
      return (EReference) dependencyTreeEClass.getEStructuralFeatures().get(1);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public DependencyModelFactory getDependencyModelFactory() {
      return (DependencyModelFactory) getEFactoryInstance();
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   private boolean isCreated = false;

   /**
    * Creates the meta-model objects for the package. This method is
    * guarded to have no affect on any invocation but its first.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void createPackageContents() {
      if (isCreated)
         return;
      isCreated = true;

      // Create classes and their features
      dependencyNodeEClass = createEClass(DEPENDENCY_NODE);
      createEReference(dependencyNodeEClass, DEPENDENCY_NODE__ARTIFACT);
      createEReference(dependencyNodeEClass, DEPENDENCY_NODE__CHILDREN);
      createEAttribute(dependencyNodeEClass, DEPENDENCY_NODE__SELECTED);
      createEReference(dependencyNodeEClass, DEPENDENCY_NODE__PARENT);
      createEAttribute(dependencyNodeEClass, DEPENDENCY_NODE__INHERITED_SCOPE);
      createEAttribute(dependencyNodeEClass, DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT);
      createEAttribute(dependencyNodeEClass, DEPENDENCY_NODE__MANAGED_SCOPE);
      createEReference(dependencyNodeEClass, DEPENDENCY_NODE__CONFLICT_NODE);
      createEReference(dependencyNodeEClass, DEPENDENCY_NODE__DECLARED_DEPENDENCY);
      createEAttribute(dependencyNodeEClass, DEPENDENCY_NODE__OPTIONAL);
      createEAttribute(dependencyNodeEClass, DEPENDENCY_NODE__CONFLICT_VERSION_CONSTRAINT);
      createEReference(dependencyNodeEClass, DEPENDENCY_NODE__CYCLE_NODE);

      dependencyModelEClass = createEClass(DEPENDENCY_MODEL);
      createEReference(dependencyModelEClass, DEPENDENCY_MODEL__ARTIFACTS);
      createEReference(dependencyModelEClass, DEPENDENCY_MODEL__DEPENDENCY_TREES);
      createEReference(dependencyModelEClass, DEPENDENCY_MODEL__ROOT_ARTIFACTS);

      dependencyTreeEClass = createEClass(DEPENDENCY_TREE);
      createEReference(dependencyTreeEClass, DEPENDENCY_TREE__ARTIFACT);
      createEReference(dependencyTreeEClass, DEPENDENCY_TREE__DEPENDENCY_NODES);
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   private boolean isInitialized = false;

   /**
    * Complete the initialization of the package and its meta-model. This
    * method is guarded to have no affect on any invocation but its first.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   public void initializePackageContents() {
      if (isInitialized)
         return;
      isInitialized = true;

      // Initialize package
      setName(eNAME);
      setNsPrefix(eNS_PREFIX);
      setNsURI(eNS_URI);

      // Obtain other dependent packages
      MavenModelPackage theMavenModelPackage = (MavenModelPackage) EPackage.Registry.INSTANCE.getEPackage(MavenModelPackage.eNS_URI);

      // Create type parameters

      // Set bounds for type parameters

      // Add supertypes to classes

      // Initialize classes and features; add operations and parameters
      initEClass(dependencyNodeEClass, DependencyNode.class, "DependencyNode", !IS_ABSTRACT, !IS_INTERFACE,
         IS_GENERATED_INSTANCE_CLASS);
      initEReference(getDependencyNode_Artifact(), theMavenModelPackage.getMavenArtifact(), null, "artifact", null, 1,
         1, DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES,
         !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEReference(getDependencyNode_Children(), this.getDependencyNode(), this.getDependencyNode_Parent(),
         "children", null, 0, -1, DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE,
         !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEAttribute(getDependencyNode_Selected(), ecorePackage.getEBoolean(), "selected", "true", 0, 1,
         DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
         !IS_DERIVED, IS_ORDERED);
      initEReference(getDependencyNode_Parent(), this.getDependencyNode(), this.getDependencyNode_Children(), "parent",
         null, 0, 1, DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE,
         !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEAttribute(getDependencyNode_InheritedScope(), theMavenModelPackage.getNullableScope(), "inheritedScope",
         null, 0, 1, DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
         IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEAttribute(getDependencyNode_ManagedVersionConstraint(), ecorePackage.getEString(),
         "managedVersionConstraint", null, 0, 1, DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE,
         !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEAttribute(getDependencyNode_ManagedScope(), theMavenModelPackage.getNullableScope(), "managedScope", null,
         0, 1, DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
         !IS_DERIVED, IS_ORDERED);
      initEReference(getDependencyNode_ConflictNode(), this.getDependencyNode(), null, "conflictNode", null, 0, 1,
         DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES,
         !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEReference(getDependencyNode_DeclaredDependency(), theMavenModelPackage.getMavenDependency(), null,
         "declaredDependency", null, 1, 1, DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE,
         IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEAttribute(getDependencyNode_Optional(), ecorePackage.getEBoolean(), "optional", null, 0, 1,
         DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
         !IS_DERIVED, IS_ORDERED);
      initEAttribute(getDependencyNode_ConflictVersionConstraint(), ecorePackage.getEString(),
         "conflictVersionConstraint", null, 0, 1, DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE,
         !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEReference(getDependencyNode_CycleNode(), this.getDependencyNode(), null, "cycleNode", null, 0, 1,
         DependencyNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES,
         !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

      addEOperation(dependencyNodeEClass, ecorePackage.getEString(), "getGroupId", 1, 1, IS_UNIQUE, IS_ORDERED);

      addEOperation(dependencyNodeEClass, ecorePackage.getEString(), "getArtifactId", 1, 1, IS_UNIQUE, IS_ORDERED);

      addEOperation(dependencyNodeEClass, theMavenModelPackage.getScope(), "getEffectiveScope", 1, 1, IS_UNIQUE,
         IS_ORDERED);

      addEOperation(dependencyNodeEClass, ecorePackage.getEString(), "getEffectiveVersionConstraint", 1, 1, IS_UNIQUE,
         IS_ORDERED);

      addEOperation(dependencyNodeEClass, ecorePackage.getEString(), "getClassifier", 0, 1, IS_UNIQUE, IS_ORDERED);

      addEOperation(dependencyNodeEClass, ecorePackage.getEString(), "getType", 1, 1, IS_UNIQUE, IS_ORDERED);

      initEClass(dependencyModelEClass, DependencyModel.class, "DependencyModel", !IS_ABSTRACT, !IS_INTERFACE,
         IS_GENERATED_INSTANCE_CLASS);
      initEReference(getDependencyModel_Artifacts(), theMavenModelPackage.getMavenArtifact(), null, "artifacts", null,
         0, -1, DependencyModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
         !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEReference(getDependencyModel_DependencyTrees(), this.getDependencyTree(), null, "dependencyTrees", null, 0,
         -1, DependencyModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
         !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEReference(getDependencyModel_RootArtifacts(), theMavenModelPackage.getMavenArtifact(), null,
         "rootArtifacts", null, 0, -1, DependencyModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE,
         !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

      EOperation op = addEOperation(dependencyModelEClass, this.getDependencyTree(), "getDependencyTree", 0, 1,
         IS_UNIQUE, IS_ORDERED);
      addEParameter(op, theMavenModelPackage.getMavenArtifact(), "artifact", 0, 1, IS_UNIQUE, IS_ORDERED);

      op = addEOperation(dependencyModelEClass, this.getDependencyTree(), "getDependencyTree", 0, 1, IS_UNIQUE,
         IS_ORDERED);
      addEParameter(op, theMavenModelPackage.getArtifactKey(), "artifactKey", 0, 1, IS_UNIQUE, IS_ORDERED);

      op = addEOperation(dependencyModelEClass, theMavenModelPackage.getMavenArtifact(), "getArtifact", 0, 1,
         IS_UNIQUE, IS_ORDERED);
      addEParameter(op, theMavenModelPackage.getArtifactKey(), "artifactKey", 0, 1, IS_UNIQUE, IS_ORDERED);

      initEClass(dependencyTreeEClass, DependencyTree.class, "DependencyTree", !IS_ABSTRACT, !IS_INTERFACE,
         IS_GENERATED_INSTANCE_CLASS);
      initEReference(getDependencyTree_Artifact(), theMavenModelPackage.getMavenArtifact(), null, "artifact", null, 1,
         1, DependencyTree.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES,
         !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
      initEReference(getDependencyTree_DependencyNodes(), this.getDependencyNode(), null, "dependencyNodes", null, 0,
         -1, DependencyTree.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
         !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

      // Create resource
      createResource(eNS_URI);
   }

} // DependencyModelPackageImpl
