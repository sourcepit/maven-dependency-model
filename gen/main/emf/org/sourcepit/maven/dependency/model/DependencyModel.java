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
import org.sourcepit.common.maven.model.ArtifactKey;
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
 * <li>{@link org.sourcepit.maven.dependency.model.DependencyModel#getRootArtifacts <em>Root Artifacts</em>}</li>
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

   /**
    * Returns the value of the '<em><b>Root Artifacts</b></em>' reference list.
    * The list contents are of type {@link org.sourcepit.common.maven.model.MavenArtifact}.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Root Artifacts</em>' reference list isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Root Artifacts</em>' reference list.
    * @see org.sourcepit.maven.dependency.model.DependencyModelPackage#getDependencyModel_RootArtifacts()
    * @model
    * @generated
    */
   EList<MavenArtifact> getRootArtifacts();

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model
    * @generated
    */
   DependencyTree getDependencyTree(MavenArtifact artifact);

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model artifactKeyDataType="org.sourcepit.common.maven.model.ArtifactKey"
    * @generated
    */
   DependencyTree getDependencyTree(ArtifactKey artifactKey);

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * 
    * @model artifactKeyDataType="org.sourcepit.common.maven.model.ArtifactKey"
    * @generated
    */
   MavenArtifact getArtifact(ArtifactKey artifactKey);

} // DependencyModel
