/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.maven.model.superpom.DefaultSuperPomProvider;
import org.apache.maven.model.superpom.SuperPomProvider;

public class FilterCentralFromSuperPom implements SuperPomProvider
{
   private final DefaultSuperPomProvider defaultProvider;
   
   @Inject
   public FilterCentralFromSuperPom(DefaultSuperPomProvider defaultProvider)
   {
      this.defaultProvider = defaultProvider;
   }

   @Override
   public Model getSuperModel(String version)
   {
      final Model superModel = defaultProvider.getSuperModel(version);
      superModel.getRepositories().clear();
      superModel.getPluginRepositories().clear();
      return superModel;
   }
}
