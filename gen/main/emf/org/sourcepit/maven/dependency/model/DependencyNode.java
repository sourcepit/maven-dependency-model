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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.sourcepit.common.maven.model.MavenArtifact;
import org.sourcepit.common.maven.model.MavenDependency;
import org.sourcepit.common.maven.model.Scope;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Dependency Node</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getArtifact <em>Artifact</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getChildren <em>Children</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#isSelected <em>Selected</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getParent <em>Parent</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getInheritedScope <em>Inherited Scope</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getManagedVersionConstraint <em>Managed Version
 * Constraint</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getManagedScope <em>Managed Scope</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getConflictNode <em>Conflict Node</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getDeclaredDependency <em>Declared Dependency</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#isOptional <em>Optional</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getConflictVersionConstraint <em>Conflict Version
 * Constraint</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyNode#getCycleNode <em>Cycle Node</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode()
 * @model
 * @generated
 */
public interface DependencyNode extends EObject
{
   /**
    * Returns the value of the '<em><b>Artifact</b></em>' reference.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Artifact</em>' reference isn't clear, there really should be more of a description
    * here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Artifact</em>' reference.
    * @see #setArtifact(MavenArtifact)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_Artifact()
    * @model required="true"
    * @generated
    */
   MavenArtifact getArtifact();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getArtifact <em>Artifact</em>}'
    * reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Artifact</em>' reference.
    * @see #getArtifact()
    * @generated
    */
   void setArtifact(MavenArtifact value);

   /**
    * Returns the value of the '<em><b>Children</b></em>' containment reference list.
    * The list contents are of type {@link org.sourcepit.maven.dependency.model.DependencyNode}.
    * It is bidirectional and its opposite is '{@link org.sourcepit.maven.dependency.model.DependencyNode#getParent
    * <em>Parent</em>}'.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Children</em>' containment reference list isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Children</em>' containment reference list.
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_Children()
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getParent
    * @model opposite="parent" containment="true"
    * @generated
    */
   EList<DependencyNode> getChildren();

   /**
    * Returns the value of the '<em><b>Selected</b></em>' attribute.
    * The default value is <code>"true"</code>.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Selected</em>' attribute isn't clear, there really should be more of a description
    * here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Selected</em>' attribute.
    * @see #setSelected(boolean)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_Selected()
    * @model default="true"
    * @generated
    */
   boolean isSelected();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#isSelected <em>Selected</em>}'
    * attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Selected</em>' attribute.
    * @see #isSelected()
    * @generated
    */
   void setSelected(boolean value);

   /**
    * Returns the value of the '<em><b>Parent</b></em>' container reference.
    * It is bidirectional and its opposite is '{@link org.sourcepit.maven.dependency.model.DependencyNode#getChildren
    * <em>Children</em>}'.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Parent</em>' container reference isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Parent</em>' container reference.
    * @see #setParent(DependencyNode)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_Parent()
    * @see org.sourcepit.maven.dependency.model.DependencyNode#getChildren
    * @model opposite="children" transient="false"
    * @generated
    */
   DependencyNode getParent();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getParent <em>Parent</em>}'
    * container reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Parent</em>' container reference.
    * @see #getParent()
    * @generated
    */
   void setParent(DependencyNode value);

   /**
    * Returns the value of the '<em><b>Inherited Scope</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Inherited Scope</em>' attribute isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Inherited Scope</em>' attribute.
    * @see #setInheritedScope(Scope)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_InheritedScope()
    * @model dataType="org.sourcepit.common.maven.model.NullableScope"
    * @generated
    */
   Scope getInheritedScope();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getInheritedScope
    * <em>Inherited Scope</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Inherited Scope</em>' attribute.
    * @see #getInheritedScope()
    * @generated
    */
   void setInheritedScope(Scope value);

   /**
    * Returns the value of the '<em><b>Managed Version Constraint</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Managed Version Constraint</em>' attribute isn't clear, there really should be more of
    * a description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Managed Version Constraint</em>' attribute.
    * @see #setManagedVersionConstraint(String)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_ManagedVersionConstraint()
    * @model
    * @generated
    */
   String getManagedVersionConstraint();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getManagedVersionConstraint
    * <em>Managed Version Constraint</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Managed Version Constraint</em>' attribute.
    * @see #getManagedVersionConstraint()
    * @generated
    */
   void setManagedVersionConstraint(String value);

   /**
    * Returns the value of the '<em><b>Managed Scope</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Managed Scope</em>' attribute isn't clear, there really should be more of a description
    * here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Managed Scope</em>' attribute.
    * @see #setManagedScope(Scope)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_ManagedScope()
    * @model dataType="org.sourcepit.common.maven.model.NullableScope"
    * @generated
    */
   Scope getManagedScope();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getManagedScope
    * <em>Managed Scope</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Managed Scope</em>' attribute.
    * @see #getManagedScope()
    * @generated
    */
   void setManagedScope(Scope value);

   /**
    * Returns the value of the '<em><b>Conflict Node</b></em>' reference.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Conflict Node</em>' reference isn't clear, there really should be more of a description
    * here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Conflict Node</em>' reference.
    * @see #setConflictNode(DependencyNode)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_ConflictNode()
    * @model
    * @generated
    */
   DependencyNode getConflictNode();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getConflictNode
    * <em>Conflict Node</em>}' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Conflict Node</em>' reference.
    * @see #getConflictNode()
    * @generated
    */
   void setConflictNode(DependencyNode value);

   /**
    * Returns the value of the '<em><b>Declared Dependency</b></em>' containment reference.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Declared Dependency</em>' containment reference isn't clear, there really should be
    * more of a description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Declared Dependency</em>' containment reference.
    * @see #setDeclaredDependency(MavenDependency)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_DeclaredDependency()
    * @model containment="true" required="true"
    * @generated
    */
   MavenDependency getDeclaredDependency();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getDeclaredDependency
    * <em>Declared Dependency</em>}' containment reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Declared Dependency</em>' containment reference.
    * @see #getDeclaredDependency()
    * @generated
    */
   void setDeclaredDependency(MavenDependency value);

   /**
    * Returns the value of the '<em><b>Optional</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Optional</em>' attribute isn't clear, there really should be more of a description
    * here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Optional</em>' attribute.
    * @see #setOptional(boolean)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_Optional()
    * @model
    * @generated
    */
   boolean isOptional();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#isOptional <em>Optional</em>}'
    * attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Optional</em>' attribute.
    * @see #isOptional()
    * @generated
    */
   void setOptional(boolean value);

   /**
    * Returns the value of the '<em><b>Conflict Version Constraint</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Conflict Version</em>' attribute isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Conflict Version Constraint</em>' attribute.
    * @see #setConflictVersionConstraint(String)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_ConflictVersionConstraint()
    * @model
    * @generated
    */
   String getConflictVersionConstraint();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getConflictVersionConstraint
    * <em>Conflict Version Constraint</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Conflict Version Constraint</em>' attribute.
    * @see #getConflictVersionConstraint()
    * @generated
    */
   void setConflictVersionConstraint(String value);

   /**
    * Returns the value of the '<em><b>Cycle Node</b></em>' reference.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Cycle</em>' reference isn't clear, there really should be more of a description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Cycle Node</em>' reference.
    * @see #setCycleNode(DependencyNode)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyNode_CycleNode()
    * @model
    * @generated
    */
   DependencyNode getCycleNode();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyNode#getCycleNode
    * <em>Cycle Node</em>}' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Cycle Node</em>' reference.
    * @see #getCycleNode()
    * @generated
    */
   void setCycleNode(DependencyNode value);

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model kind="operation" required="true"
    * @generated
    */
   String getGroupId();

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model kind="operation" required="true"
    * @generated
    */
   String getArtifactId();

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model kind="operation" required="true"
    * @generated
    */
   Scope getEffectiveScope();

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model kind="operation" required="true"
    * @generated
    */
   String getEffectiveVersionConstraint();

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model kind="operation"
    * @generated
    */
   String getClassifier();

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model kind="operation" required="true"
    * @generated
    */
   String getType();

} // DependencyNode
