/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyTree#getTargetArtifact <em>Target Artifact</em>}</li>
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
    * Returns the value of the '<em><b>Target Artifact</b></em>' reference.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Target Artifact</em>' reference isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Target Artifact</em>' reference.
    * @see #setTargetArtifact(MavenArtifact)
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyTree_TargetArtifact()
    * @model required="true"
    * @generated
    */
   MavenArtifact getTargetArtifact();

   /**
    * Sets the value of the '{@link org.sourcepit.maven.dependency.model.DependencyTree#getTargetArtifact
    * <em>Target Artifact</em>}' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Target Artifact</em>' reference.
    * @see #getTargetArtifact()
    * @generated
    */
   void setTargetArtifact(MavenArtifact value);

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
