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

package org.sourcepit.maven.dependency.model.aether;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.maven.dependency.model.aether.DependencyNodeTraverser.Visit;

public class DependencyNodeTraverserTest extends EmbeddedMavenEnvironmentTest {
   @Rule
   public TestName name = new TestName();

   @Test
   public void testSimple() throws Exception {
      final List<Visit> visits = traverse(null);
      assertEquals(4, visits.size());
      assertEquals("null", visits.get(0).toString());
      assertEquals("a:A:jar:1 (compile)", visits.get(1).toString());
      assertEquals("c:C:jar:1 (compile)", visits.get(2).toString());
      assertEquals("b:B:jar:1 (compile)", visits.get(3).toString());
   }

   @Test
   public void testCycle1() throws Exception {
      final List<Visit> visits = traverse(null);
      assertEquals(4, visits.size());
      assertEquals("null", visits.get(0).toString());
      assertEquals("a:A:jar:1 (compile)", visits.get(1).toString());
      assertEquals("b:B:jar:1 (compile)", visits.get(2).toString());
      assertEquals("a:A:jar:1 (compile) (cyclic)", visits.get(3).toString());
   }

   @Test
   public void testCycle2() throws Exception {
      final List<Visit> visits = traverse(null);
      assertEquals(4, visits.size());
      assertEquals("null", visits.get(0).toString());
      assertEquals("a:A:jar:1 (compile)", visits.get(1).toString());
      assertEquals("b:B:jar:1 (compile)", visits.get(2).toString());
      assertEquals("a:A:jar:1 (compile) (cyclic)", visits.get(3).toString());
   }

   @Test
   public void testReplaced1() throws Exception {
      DependencyNodeChooser nodeChooser = new NearestDependencyNodeChooser(false);

      DependencyGraphTransformer transformer = new ChainedDependencyGraphTransformer(
         new DependencyNode2AdapterTransformer(true), new HideDuplicatedSiblings(), new ApplyScopeAndOptional(),
         new VersionConflictResolver(nodeChooser));

      List<Visit> visits = traverse(transformer);
      assertEquals(5, visits.size());
      assertEquals("null", visits.get(0).toString());
      assertEquals("a:A:jar:1 (compile)", visits.get(1).toString());
      assertEquals("b:B:jar:1 (compile) -> b:B:jar:2 (compile)", visits.get(2).toString());
      assertEquals("b:B:jar:2 (compile)", visits.get(3).toString());
      assertEquals("c:C:jar:2 (compile)", visits.get(4).toString());

      DependencyNode nodeA = visits.get(1).getNode();
      DependencyNode nodeB1 = visits.get(2).getNode();
      DependencyNode nodeB2 = visits.get(3).getNode();
      DependencyNode nodeC = visits.get(4).getNode();

      visits = traverseGraph(nodeA);
      assertEquals(3, visits.size());
      assertEquals("a:A:jar:1 (compile)", visits.get(0).toString());
      assertEquals("b:B:jar:1 (compile) -> b:B:jar:2 (compile)", visits.get(1).toString());
      assertEquals("c:C:jar:2 (compile)", visits.get(2).toString());

      visits = traverseGraph(nodeB1);
      assertEquals(2, visits.size());
      assertEquals("b:B:jar:1 (compile) -> b:B:jar:2 (compile)", visits.get(0).toString());
      assertEquals("c:C:jar:2 (compile)", visits.get(1).toString());

      visits = traverseGraph(nodeB2);
      assertEquals(2, visits.size());
      assertEquals("b:B:jar:2 (compile)", visits.get(0).toString());
      assertEquals("c:C:jar:2 (compile)", visits.get(1).toString());

      visits = traverseGraph(nodeC);
      assertEquals(1, visits.size());
      assertEquals("c:C:jar:2 (compile)", visits.get(0).toString());
   }

   @Test
   public void testReplaced2() throws Exception {
      DependencyNodeChooser nodeChooser = new NearestDependencyNodeChooser(false);

      DependencyGraphTransformer transformer = new ChainedDependencyGraphTransformer(
         new DependencyNode2AdapterTransformer(true), new HideDuplicatedSiblings(), new ApplyScopeAndOptional(),
         new VersionConflictResolver(nodeChooser));

      List<Visit> visits = traverse(transformer);
      assertEquals(4, visits.size());
      assertEquals("null", visits.get(0).toString());
      assertEquals("a:A:jar:1 (compile)", visits.get(1).toString());
      assertEquals("b:B:jar:1 (compile)", visits.get(2).toString());
      assertEquals("a:A:jar:2 (compile) -> a:A:jar:1 (compile) (cyclic)", visits.get(3).toString());

      DependencyNode nodeA = visits.get(1).getNode();
      DependencyNode nodeB = visits.get(2).getNode();
      DependencyNode nodeA2 = visits.get(3).getNode();

      visits = traverseGraph(nodeA);
      assertEquals(3, visits.size());
      assertEquals("a:A:jar:1 (compile)", visits.get(0).toString());
      assertEquals("b:B:jar:1 (compile)", visits.get(1).toString());
      assertEquals("a:A:jar:2 (compile) -> a:A:jar:1 (compile) (cyclic)", visits.get(2).toString());

      visits = traverseGraph(nodeB);
      assertEquals(3, visits.size());
      assertEquals("b:B:jar:1 (compile)", visits.get(0).toString());
      assertEquals("a:A:jar:2 (compile) -> a:A:jar:1 (compile)", visits.get(1).toString());
      assertEquals("b:B:jar:1 (compile) (cyclic)", visits.get(2).toString());

      visits = traverseGraph(nodeA2);
      assertEquals(3, visits.size());
      assertEquals("a:A:jar:2 (compile) -> a:A:jar:1 (compile)", visits.get(0).toString());
      assertEquals("b:B:jar:1 (compile)", visits.get(1).toString());
      assertEquals("a:A:jar:2 (compile) -> a:A:jar:1 (compile) (cyclic)", visits.get(2).toString());
   }

   private List<Visit> traverse(DependencyGraphTransformer transformer) throws Exception {
      final String resource = getClass().getSimpleName() + "/" + getTestName() + ".txt";
      DependencyNode graph = parseDependencyGraph(resource);
      if (transformer != null) {
         graph = transformer.transformGraph(graph, null);
      }
      return traverseGraph(graph);
   }

   private List<Visit> traverseGraph(DependencyNode graph) {
      final List<Visit> visits = new ArrayList<Visit>();
      DependencyNodeTraverser traverser = new DependencyNodeTraverser(false) {
         @Override
         protected boolean visitEnter(Visit visit, PropertiesMap parentProps) {
            visits.add(visit);
            return super.visitEnter(visit, parentProps);
         }
      };
      traverser.traverse(graph);
      return visits;
   }

   private DependencyNode parseDependencyGraph(String resource) throws IOException {
      return new DependencyGraphParser().parse(resource);
   }

   protected String getTestName() {
      return name.getMethodName().substring("test".length());
   }

   @Override
   protected Environment newEnvironment() {
      return Environment.get("env-test.properties");
   }
}
