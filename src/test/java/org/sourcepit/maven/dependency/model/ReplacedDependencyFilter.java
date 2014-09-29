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

import java.util.List;

import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.sourcepit.maven.dependency.model.aether.DependencyNode2;
import org.sourcepit.maven.dependency.model.aether.DependencyNode2Adapter;

public class ReplacedDependencyFilter implements DependencyFilter
{

   @Override
   public boolean accept(DependencyNode node, List<DependencyNode> parents)
   {
      final DependencyNode2 node2 = DependencyNode2Adapter.get(node);
      return node2.getReplacement() == null;
   }

}
