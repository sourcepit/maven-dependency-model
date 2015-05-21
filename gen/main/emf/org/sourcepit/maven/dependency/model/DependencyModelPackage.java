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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @see org.sourcepit.maven.dependency.model.DependencyModelFactory
 * @model kind="package"
 * @generated
 */
public interface DependencyModelPackage extends EPackage {
   /**
    * The package name.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   String eNAME = "model";

   /**
    * The package namespace URI.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   String eNS_URI = "http://www.sourcepit.org/maven/dependency/model/0.1";

   /**
    * The package namespace name.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   String eNS_PREFIX = "maven-dependency-model";

   /**
    * The singleton instance of the package.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   DependencyModelPackage eINSTANCE = org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl.init();

   /**
    * The meta object id for the '{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl
    * <em>Dependency Node</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl
    * @see org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl#getDependencyNode()
    * @generated
    */
   int DEPENDENCY_NODE = 0;

   /**
    * The feature id for the '<em><b>Artifact</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__ARTIFACT = 0;

   /**
    * The feature id for the '<em><b>Children</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__CHILDREN = 1;

   /**
    * The feature id for the '<em><b>Selected</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__SELECTED = 2;

   /**
    * The feature id for the '<em><b>Parent</b></em>' container reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__PARENT = 3;

   /**
    * The feature id for the '<em><b>Inherited Scope</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__INHERITED_SCOPE = 4;

   /**
    * The feature id for the '<em><b>Managed Version Constraint</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT = 5;

   /**
    * The feature id for the '<em><b>Managed Scope</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__MANAGED_SCOPE = 6;

   /**
    * The feature id for the '<em><b>Conflict Node</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__CONFLICT_NODE = 7;

   /**
    * The feature id for the '<em><b>Declared Dependency</b></em>' containment reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__DECLARED_DEPENDENCY = 8;

   /**
    * The feature id for the '<em><b>Optional</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__OPTIONAL = 9;

   /**
    * The feature id for the '<em><b>Conflict Version Constraint</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__CONFLICT_VERSION_CONSTRAINT = 10;

   /**
    * The feature id for the '<em><b>Cycle Node</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__CYCLE_NODE = 11;

   /**
    * The number of structural features of the '<em>Dependency Node</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE_FEATURE_COUNT = 12;

   /**
    * The meta object id for the '{@link org.sourcepit.maven.dependency.model.impl.DependencyModelImpl
    * <em>Dependency Model</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see org.sourcepit.maven.dependency.model.impl.DependencyModelImpl
    * @see org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl#getDependencyModel()
    * @generated
    */
   int DEPENDENCY_MODEL = 1;

   /**
    * The feature id for the '<em><b>Artifacts</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_MODEL__ARTIFACTS = 0;

   /**
    * The feature id for the '<em><b>Dependency Trees</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_MODEL__DEPENDENCY_TREES = 1;

   /**
    * The feature id for the '<em><b>Root Artifacts</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_MODEL__ROOT_ARTIFACTS = 2;

   /**
    * The number of structural features of the '<em>Dependency Model</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_MODEL_FEATURE_COUNT = 3;

   /**
    * The meta object id for the '{@link org.sourcepit.maven.dependency.model.impl.DependencyTreeImpl
    * <em>Dependency Tree</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see org.sourcepit.maven.dependency.model.impl.DependencyTreeImpl
    * @see org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl#getDependencyTree()
    * @generated
    */
   int DEPENDENCY_TREE = 2;

   /**
    * The feature id for the '<em><b>Artifact</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_TREE__ARTIFACT = 0;

   /**
    * The feature id for the '<em><b>Dependency Nodes</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_TREE__DEPENDENCY_NODES = 1;

   /**
    * The number of structural features of the '<em>Dependency Tree</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_TREE_FEATURE_COUNT = 2;


   /**
    * Returns the meta object for class '{@link org.sourcepit.maven.dependency.model.DependencyNode
    * <em>Dependency Node</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for class '<em>Dependency Node</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode
    * @generated
    */
   EClass getDependencyNode();

   /**
    * Returns the meta object for the reference '{@link org.sourcepit.maven.dependency.model.DependencyNode#getArtifact
    * <em>Artifact</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the reference '<em>Artifact</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getArtifact()
    * @see #getDependencyNode()
    * @generated
    */
   EReference getDependencyNode_Artifact();

   /**
    * Returns the meta object for the containment reference list '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getChildren <em>Children</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the containment reference list '<em>Children</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getChildren()
    * @see #getDependencyNode()
    * @generated
    */
   EReference getDependencyNode_Children();

   /**
    * Returns the meta object for the attribute '{@link org.sourcepit.maven.dependency.model.DependencyNode#isSelected
    * <em>Selected</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the attribute '<em>Selected</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#isSelected()
    * @see #getDependencyNode()
    * @generated
    */
   EAttribute getDependencyNode_Selected();

   /**
    * Returns the meta object for the container reference '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getParent <em>Parent</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the container reference '<em>Parent</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getParent()
    * @see #getDependencyNode()
    * @generated
    */
   EReference getDependencyNode_Parent();

   /**
    * Returns the meta object for the attribute '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getInheritedScope <em>Inherited Scope</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the attribute '<em>Inherited Scope</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getInheritedScope()
    * @see #getDependencyNode()
    * @generated
    */
   EAttribute getDependencyNode_InheritedScope();

   /**
    * Returns the meta object for the attribute '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getManagedVersionConstraint
    * <em>Managed Version Constraint</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the attribute '<em>Managed Version Constraint</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getManagedVersionConstraint()
    * @see #getDependencyNode()
    * @generated
    */
   EAttribute getDependencyNode_ManagedVersionConstraint();

   /**
    * Returns the meta object for the attribute '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getManagedScope <em>Managed Scope</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the attribute '<em>Managed Scope</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getManagedScope()
    * @see #getDependencyNode()
    * @generated
    */
   EAttribute getDependencyNode_ManagedScope();

   /**
    * Returns the meta object for the reference '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getConflictNode <em>Conflict Node</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the reference '<em>Conflict Node</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getConflictNode()
    * @see #getDependencyNode()
    * @generated
    */
   EReference getDependencyNode_ConflictNode();

   /**
    * Returns the meta object for the containment reference '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getDeclaredDependency <em>Declared Dependency</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the containment reference '<em>Declared Dependency</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getDeclaredDependency()
    * @see #getDependencyNode()
    * @generated
    */
   EReference getDependencyNode_DeclaredDependency();

   /**
    * Returns the meta object for the attribute '{@link org.sourcepit.maven.dependency.model.DependencyNode#isOptional
    * <em>Optional</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the attribute '<em>Optional</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#isOptional()
    * @see #getDependencyNode()
    * @generated
    */
   EAttribute getDependencyNode_Optional();

   /**
    * Returns the meta object for the attribute '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getConflictVersionConstraint
    * <em>Conflict Version Constraint</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the attribute '<em>Conflict Version Constraint</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getConflictVersionConstraint()
    * @see #getDependencyNode()
    * @generated
    */
   EAttribute getDependencyNode_ConflictVersionConstraint();

   /**
    * Returns the meta object for the reference '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getCycleNode <em>Cycle Node</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the reference '<em>Cycle Node</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getCycleNode()
    * @see #getDependencyNode()
    * @generated
    */
   EReference getDependencyNode_CycleNode();

   /**
    * Returns the meta object for class '{@link org.sourcepit.maven.dependency.model.DependencyModel
    * <em>Dependency Model</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for class '<em>Dependency Model</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyModel
    * @generated
    */
   EClass getDependencyModel();

   /**
    * Returns the meta object for the containment reference list '
    * {@link org.sourcepit.maven.dependency.model.DependencyModel#getArtifacts <em>Artifacts</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the containment reference list '<em>Artifacts</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyModel#getArtifacts()
    * @see #getDependencyModel()
    * @generated
    */
   EReference getDependencyModel_Artifacts();

   /**
    * Returns the meta object for the containment reference list '
    * {@link org.sourcepit.maven.dependency.model.DependencyModel#getDependencyTrees <em>Dependency Trees</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the containment reference list '<em>Dependency Trees</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyModel#getDependencyTrees()
    * @see #getDependencyModel()
    * @generated
    */
   EReference getDependencyModel_DependencyTrees();

   /**
    * Returns the meta object for the reference list '
    * {@link org.sourcepit.maven.dependency.model.DependencyModel#getRootArtifacts <em>Root Artifacts</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the reference list '<em>Root Artifacts</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyModel#getRootArtifacts()
    * @see #getDependencyModel()
    * @generated
    */
   EReference getDependencyModel_RootArtifacts();

   /**
    * Returns the meta object for class '{@link org.sourcepit.maven.dependency.model.DependencyTree
    * <em>Dependency Tree</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for class '<em>Dependency Tree</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyTree
    * @generated
    */
   EClass getDependencyTree();

   /**
    * Returns the meta object for the reference '{@link org.sourcepit.maven.dependency.model.DependencyTree#getArtifact
    * <em>Artifact</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the reference '<em>Artifact</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyTree#getArtifact()
    * @see #getDependencyTree()
    * @generated
    */
   EReference getDependencyTree_Artifact();

   /**
    * Returns the meta object for the containment reference list '
    * {@link org.sourcepit.maven.dependency.model.DependencyTree#getDependencyNodes <em>Dependency Nodes</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the containment reference list '<em>Dependency Nodes</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyTree#getDependencyNodes()
    * @see #getDependencyTree()
    * @generated
    */
   EReference getDependencyTree_DependencyNodes();

   /**
    * Returns the factory that creates the instances of the model.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the factory that creates the instances of the model.
    * @generated
    */
   DependencyModelFactory getDependencyModelFactory();

   /**
    * <!-- begin-user-doc -->
    * Defines literals for the meta objects that represent
    * <ul>
    * <li>each class,</li>
    * <li>each feature of each class,</li>
    * <li>each enum,</li>
    * <li>and each data type</li>
    * </ul>
    * <!-- end-user-doc -->
    * 
    * @generated
    */
   interface Literals {
      /**
       * The meta object literal for the '{@link org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl
       * <em>Dependency Node</em>}' class.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @see org.sourcepit.maven.dependency.model.impl.DependencyNodeImpl
       * @see org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl#getDependencyNode()
       * @generated
       */
      EClass DEPENDENCY_NODE = eINSTANCE.getDependencyNode();

      /**
       * The meta object literal for the '<em><b>Artifact</b></em>' reference feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_NODE__ARTIFACT = eINSTANCE.getDependencyNode_Artifact();

      /**
       * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_NODE__CHILDREN = eINSTANCE.getDependencyNode_Children();

      /**
       * The meta object literal for the '<em><b>Selected</b></em>' attribute feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EAttribute DEPENDENCY_NODE__SELECTED = eINSTANCE.getDependencyNode_Selected();

      /**
       * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_NODE__PARENT = eINSTANCE.getDependencyNode_Parent();

      /**
       * The meta object literal for the '<em><b>Inherited Scope</b></em>' attribute feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EAttribute DEPENDENCY_NODE__INHERITED_SCOPE = eINSTANCE.getDependencyNode_InheritedScope();

      /**
       * The meta object literal for the '<em><b>Managed Version Constraint</b></em>' attribute feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EAttribute DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT = eINSTANCE.getDependencyNode_ManagedVersionConstraint();

      /**
       * The meta object literal for the '<em><b>Managed Scope</b></em>' attribute feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EAttribute DEPENDENCY_NODE__MANAGED_SCOPE = eINSTANCE.getDependencyNode_ManagedScope();

      /**
       * The meta object literal for the '<em><b>Conflict Node</b></em>' reference feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_NODE__CONFLICT_NODE = eINSTANCE.getDependencyNode_ConflictNode();

      /**
       * The meta object literal for the '<em><b>Declared Dependency</b></em>' containment reference feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_NODE__DECLARED_DEPENDENCY = eINSTANCE.getDependencyNode_DeclaredDependency();

      /**
       * The meta object literal for the '<em><b>Optional</b></em>' attribute feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EAttribute DEPENDENCY_NODE__OPTIONAL = eINSTANCE.getDependencyNode_Optional();

      /**
       * The meta object literal for the '<em><b>Conflict Version Constraint</b></em>' attribute feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EAttribute DEPENDENCY_NODE__CONFLICT_VERSION_CONSTRAINT = eINSTANCE.getDependencyNode_ConflictVersionConstraint();

      /**
       * The meta object literal for the '<em><b>Cycle Node</b></em>' reference feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_NODE__CYCLE_NODE = eINSTANCE.getDependencyNode_CycleNode();

      /**
       * The meta object literal for the '{@link org.sourcepit.maven.dependency.model.impl.DependencyModelImpl
       * <em>Dependency Model</em>}' class.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @see org.sourcepit.maven.dependency.model.impl.DependencyModelImpl
       * @see org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl#getDependencyModel()
       * @generated
       */
      EClass DEPENDENCY_MODEL = eINSTANCE.getDependencyModel();

      /**
       * The meta object literal for the '<em><b>Artifacts</b></em>' containment reference list feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_MODEL__ARTIFACTS = eINSTANCE.getDependencyModel_Artifacts();

      /**
       * The meta object literal for the '<em><b>Dependency Trees</b></em>' containment reference list feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_MODEL__DEPENDENCY_TREES = eINSTANCE.getDependencyModel_DependencyTrees();

      /**
       * The meta object literal for the '<em><b>Root Artifacts</b></em>' reference list feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_MODEL__ROOT_ARTIFACTS = eINSTANCE.getDependencyModel_RootArtifacts();

      /**
       * The meta object literal for the '{@link org.sourcepit.maven.dependency.model.impl.DependencyTreeImpl
       * <em>Dependency Tree</em>}' class.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @see org.sourcepit.maven.dependency.model.impl.DependencyTreeImpl
       * @see org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl#getDependencyTree()
       * @generated
       */
      EClass DEPENDENCY_TREE = eINSTANCE.getDependencyTree();

      /**
       * The meta object literal for the '<em><b>Artifact</b></em>' reference feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_TREE__ARTIFACT = eINSTANCE.getDependencyTree_Artifact();

      /**
       * The meta object literal for the '<em><b>Dependency Nodes</b></em>' containment reference list feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_TREE__DEPENDENCY_NODES = eINSTANCE.getDependencyTree_DependencyNodes();

   }

} // DependencyModelPackage
