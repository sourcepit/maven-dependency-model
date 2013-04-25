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
 * A representation of the model object '<em><b>Dependency Model</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyModel#getArtifacts <em>Artifacts</em>}</li>
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyModel#getDependencyTrees <em>Dependency Trees</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyModel()
 * @model
 * @generated
 */
public interface DependencyModel extends EObject
{
   /**
    * Returns the value of the '<em><b>Artifacts</b></em>' containment reference list.
    * The list contents are of type {@link org.sourcepit.common.maven.model.MavenArtifact}.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Artifacts</em>' containment reference list isn't clear, there really should be more of
    * a description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Artifacts</em>' containment reference list.
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyModel_Artifacts()
    * @model containment="true"
    * @generated
    */
   EList<MavenArtifact> getArtifacts();

   /**
    * Returns the value of the '<em><b>Dependency Trees</b></em>' containment reference list.
    * The list contents are of type {@link org.sourcepit.maven.dependency.model.DependencyTree}.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Dependency Trees</em>' containment reference list isn't clear, there really should be
    * more of a description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Dependency Trees</em>' containment reference list.
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyModel_DependencyTrees()
    * @model containment="true"
    * @generated
    */
   EList<DependencyTree> getDependencyTrees();

} // DependencyModel
