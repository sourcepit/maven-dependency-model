/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.sourcepit.common.maven.model.MavenModelPackage;

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
public interface DependencyModelPackage extends EPackage
{
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
    * The feature id for the '<em><b>Dependency Declaration</b></em>' containment reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__DEPENDENCY_DECLARATION = 3;

   /**
    * The feature id for the '<em><b>Parent</b></em>' container reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__PARENT = 4;

   /**
    * The feature id for the '<em><b>Inherited Scope</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__INHERITED_SCOPE = 5;

   /**
    * The feature id for the '<em><b>Managed Version Constraint</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__MANAGED_VERSION_CONSTRAINT = 6;

   /**
    * The feature id for the '<em><b>Managed Scope</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__MANAGED_SCOPE = 7;

   /**
    * The feature id for the '<em><b>Conflict Node</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__CONFLICT_NODE = 8;

   /**
    * The feature id for the '<em><b>Version</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE__VERSION = 9;

   /**
    * The number of structural features of the '<em>Dependency Node</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_NODE_FEATURE_COUNT = 10;

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
    * The number of structural features of the '<em>Dependency Model</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_MODEL_FEATURE_COUNT = 2;

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
    * The feature id for the '<em><b>Target Artifact</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DEPENDENCY_TREE__TARGET_ARTIFACT = 0;

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
    * The meta object id for the '{@link org.sourcepit.maven.dependency.model.impl.DeclaredDependencyImpl
    * <em>Declared Dependency</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @see org.sourcepit.maven.dependency.model.impl.DeclaredDependencyImpl
    * @see org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl#getDeclaredDependency()
    * @generated
    */
   int DECLARED_DEPENDENCY = 3;

   /**
    * The feature id for the '<em><b>Group Id</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DECLARED_DEPENDENCY__GROUP_ID = MavenModelPackage.DEPENDENCY_DECLARATION__GROUP_ID;

   /**
    * The feature id for the '<em><b>Artifact Id</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DECLARED_DEPENDENCY__ARTIFACT_ID = MavenModelPackage.DEPENDENCY_DECLARATION__ARTIFACT_ID;

   /**
    * The feature id for the '<em><b>Classifier</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DECLARED_DEPENDENCY__CLASSIFIER = MavenModelPackage.DEPENDENCY_DECLARATION__CLASSIFIER;

   /**
    * The feature id for the '<em><b>Type</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DECLARED_DEPENDENCY__TYPE = MavenModelPackage.DEPENDENCY_DECLARATION__TYPE;

   /**
    * The feature id for the '<em><b>Version Constraint</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DECLARED_DEPENDENCY__VERSION_CONSTRAINT = MavenModelPackage.DEPENDENCY_DECLARATION__VERSION_CONSTRAINT;

   /**
    * The feature id for the '<em><b>Scope</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DECLARED_DEPENDENCY__SCOPE = MavenModelPackage.DEPENDENCY_DECLARATION__SCOPE;

   /**
    * The feature id for the '<em><b>Optional</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DECLARED_DEPENDENCY__OPTIONAL = MavenModelPackage.DEPENDENCY_DECLARATION__OPTIONAL;

   /**
    * The number of structural features of the '<em>Declared Dependency</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @generated
    * @ordered
    */
   int DECLARED_DEPENDENCY_FEATURE_COUNT = MavenModelPackage.DEPENDENCY_DECLARATION_FEATURE_COUNT + 0;


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
    * Returns the meta object for the containment reference '
    * {@link org.sourcepit.maven.dependency.model.DependencyNode#getDependencyDeclaration
    * <em>Dependency Declaration</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the containment reference '<em>Dependency Declaration</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getDependencyDeclaration()
    * @see #getDependencyNode()
    * @generated
    */
   EReference getDependencyNode_DependencyDeclaration();

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
    * Returns the meta object for the attribute '{@link org.sourcepit.maven.dependency.model.DependencyNode#getVersion
    * <em>Version</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the attribute '<em>Version</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getVersion()
    * @see #getDependencyNode()
    * @generated
    */
   EAttribute getDependencyNode_Version();

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
    * Returns the meta object for the reference '
    * {@link org.sourcepit.maven.dependency.model.DependencyTree#getTargetArtifact <em>Target Artifact</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for the reference '<em>Target Artifact</em>'.
    * @see org.sourcepit.maven.dependency.model.DependencyTree#getTargetArtifact()
    * @see #getDependencyTree()
    * @generated
    */
   EReference getDependencyTree_TargetArtifact();

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
    * Returns the meta object for class '{@link org.sourcepit.maven.dependency.model.DeclaredDependency
    * <em>Declared Dependency</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @return the meta object for class '<em>Declared Dependency</em>'.
    * @see org.sourcepit.maven.dependency.model.DeclaredDependency
    * @generated
    */
   EClass getDeclaredDependency();

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
   interface Literals
   {
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
       * The meta object literal for the '<em><b>Dependency Declaration</b></em>' containment reference feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_NODE__DEPENDENCY_DECLARATION = eINSTANCE.getDependencyNode_DependencyDeclaration();

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
       * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EAttribute DEPENDENCY_NODE__VERSION = eINSTANCE.getDependencyNode_Version();

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
       * The meta object literal for the '<em><b>Target Artifact</b></em>' reference feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_TREE__TARGET_ARTIFACT = eINSTANCE.getDependencyTree_TargetArtifact();

      /**
       * The meta object literal for the '<em><b>Dependency Nodes</b></em>' containment reference list feature.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @generated
       */
      EReference DEPENDENCY_TREE__DEPENDENCY_NODES = eINSTANCE.getDependencyTree_DependencyNodes();

      /**
       * The meta object literal for the '{@link org.sourcepit.maven.dependency.model.impl.DeclaredDependencyImpl
       * <em>Declared Dependency</em>}' class.
       * <!-- begin-user-doc -->
       * <!-- end-user-doc -->
       * 
       * @see org.sourcepit.maven.dependency.model.impl.DeclaredDependencyImpl
       * @see org.sourcepit.maven.dependency.model.impl.DependencyModelPackageImpl#getDeclaredDependency()
       * @generated
       */
      EClass DECLARED_DEPENDENCY = eINSTANCE.getDeclaredDependency();

   }

} // DependencyModelPackage
