/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.aether.graph.DependencyNode;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public final class DependencyUtils
{
   private static final Map<String, Map<String, String>> SCOPE_INHERITANCE = createScopeMatrix();

   private static final Set<String> SCOPES = createScopes();

   private DependencyUtils()
   {
      super();
   }

   private static Set<String> createScopes()
   {
      final Set<String> scopes = new HashSet<String>();
      scopes.add("compile");
      scopes.add("provided");
      scopes.add("runtime");
      scopes.add("test");
      scopes.add("system");
      return scopes;
   }

   public static String getEffectiveScope(String parent, String current)
   {
      return getEffectiveScope(parent, current, true);
   }

   public static String getEffectiveScope(String parent, String current, boolean erase)
   {
      checkArgument(parent != null, "Parent scope may not be null.");
      checkArgument(!"system".equals(parent) && SCOPES.contains(parent), "'%s' is no valid parent scope.", parent);
      checkArgument(SCOPES.contains(parent), "'%s' is no valid scope.", parent);

      if (parent == null)
      {
         return null;
      }

      final String scope = SCOPE_INHERITANCE.get(parent).get(current);

      return scope == null && !erase ? current : scope;
   }

   private static Map<String, Map<String, String>> createScopeMatrix()
   {
      final Map<String, Map<String, String>> matrix = new HashMap<String, Map<String, String>>();

      final Map<String, String> compile = new HashMap<String, String>();
      compile.put("compile", "compile");
      // compile.put("provided", "provided");
      compile.put("runtime", "runtime");
      // compile.put("test", "test");
      // compile.put("system", "system");

      final Map<String, String> provided = new HashMap<String, String>();
      provided.put("compile", "provided");
      // provided.put("provided", "provided");
      provided.put("runtime", "provided");
      // provided.put("test", "test");
      // provided.put("system", "system");

      final Map<String, String> runtime = new HashMap<String, String>();
      runtime.put("compile", "runtime");
      // runtime.put("provided", "provided");
      runtime.put("runtime", "runtime");
      // runtime.put("test", "test");
      // runtime.put("system", "system");

      final Map<String, String> test = new HashMap<String, String>();
      test.put("compile", "test");
      // test.put("provided", "provided");
      test.put("runtime", "test");
      // test.put("test", "test");
      // test.put("system", "system");

      // final Map<String, String> system = new HashMap<String, String>();
      // system.put("compile", "system");
      // system.put("provided", "system");
      // system.put("runtime", "system");
      // system.put("test", "test");
      // system.put("system", "system");

      matrix.put("compile", compile);
      matrix.put("provided", provided);
      matrix.put("runtime", runtime);
      matrix.put("test", test);
      // matrix.put("system", system);

      return matrix;
   }

   // public static String getEffectiveScope(String parent, String current)
   // {
   // if (parent == null)
   // {
   // return null;
   // }
   // if ("compile".equals(parent) && "compile".equals(current))
   // {
   // return "compile";
   // }
   // if ("test".equals(current) || "provided".equals(current) || "system".equals(current))
   // {
   // return null;
   // }
   // if ("provided".equals(parent) && !"test".equals(current))
   // {
   // return "provided";
   // }
   // if ("system".equals(parent) && !"test".equals(current))
   // {
   // return "system";
   // }
   // if ("runtime".equals(parent) && "runtime".equals(current) || "compile".equals(parent)
   // && "runtime".equals(current) || "runtime".equals(parent) && "compile".equals(current))
   // {
   // return "runtime";
   // }
   // return "test";
   // }

   public static Collection<List<DependencyNode>> computeConflictingNodeGroups(DependencyNode node)
   {
      final Collection<Collection<String>> conflictKeyGroups = computeConflictKeyGroups(node);

      final Multimap<Collection<String>, DependencyNode> conflictNodes = LinkedHashMultimap.create();
      node.accept(new AbstractDependencyVisitor()
      {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
            final String originGroupKey = adapter.getDependencyConflictKey();
            if (originGroupKey != null)
            {
               boolean put = false;
               for (Collection<String> conflictGroup : conflictKeyGroups)
               {
                  if (conflictGroup.contains(originGroupKey))
                  {
                     checkState(!put);
                     conflictNodes.put(conflictGroup, node);
                     put = true;
                  }
               }
               checkState(put);
            }
            return true;
         }
      });

      final Collection<List<DependencyNode>> conflictNodeGroups = new ArrayList<List<DependencyNode>>();

      for (Collection<DependencyNode> conflictNodeGroup : conflictNodes.asMap().values())
      {
         if (conflictNodeGroup.size() > 1)
         {
            conflictNodeGroups.add(new ArrayList<DependencyNode>(conflictNodeGroup));
         }
      }

      return conflictNodeGroups;
   }

   private static Collection<Collection<String>> computeConflictKeyGroups(DependencyNode node)
   {
      final Collection<Collection<String>> existingGroups = new ArrayList<Collection<String>>();
      node.accept(new AbstractDependencyVisitor()
      {
         @Override
         protected boolean onVisitEnter(DependencyNode parent, DependencyNode node)
         {
            final DependencyNode2 adapter = DependencyNode2Adapter.get(node);

            final Set<String> conflictKeys = adapter.getConflictKeys();

            for (Iterator<Collection<String>> it = existingGroups.iterator(); it.hasNext();)
            {
               final Collection<String> existing = it.next();
               if (containsAny(existing, conflictKeys))
               {
                  conflictKeys.addAll(existing);
                  it.remove();
               }
            }

            existingGroups.add(conflictKeys);

            return true;
         }

         private boolean containsAny(Collection<?> collection, Collection<?> elements)
         {
            for (Object element : elements)
            {
               if (collection.contains(element))
               {
                  return true;
               }
            }
            return false;
         }
      });

      return existingGroups;
   }
}
