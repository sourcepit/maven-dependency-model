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

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Dependency Tree</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyTree#getArtifact <em>Artifact</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyTree#getDependencyNodes <em>Dependency Nodes</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyTree()
 * @model
 * @generated
 */
public interface DependencyTree extends EObject
{
   /**
    * Returns the value of the '<em><b>Artifact</b></em>' reference.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Target Artifact</em>' reference isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Artifact</em>' reference.
    * @see #setArtifact(MavenArtifact)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyTree_Artifact()
    * @model required="true"
    * @generated
    */
   MavenArtifact getArtifact();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyTree#getArtifact <em>Artifact</em>}'
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
    * Returns the value of the '<em><b>Dependency Nodes</b></em>' containment reference list.
    * The list contents are of type {@link org.sourcepit.maven.dependency.model.DependencyNode}.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Dependency Nodes</em>' containment reference list isn't clear, there really should be
    * more of a description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Dependency Nodes</em>' containment reference list.
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyTree_DependencyNodes()
    * @model containment="true"
    * @generated
    */
   EList<DependencyNode> getDependencyNodes();

} // DependencyTree
