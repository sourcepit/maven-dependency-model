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

package org.sourcepit.maven.dependency.aether;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.sourcepit.common.constraints.NotNull;

public class DependencyGraphTransformationContextImpl implements DependencyGraphTransformationContext
{
   private final RepositorySystemSession session;

   private final Map<Object, Object> map = new HashMap<Object, Object>();

   public DependencyGraphTransformationContextImpl(RepositorySystemSession session)
   {
      this.session = session;
   }

   public RepositorySystemSession getSession()
   {
      return session;
   }

   public Object get(@NotNull Object key)
   {
      return map.get(key);
   }

   public Object put(@NotNull Object key, Object value)
   {
      if (value != null)
      {
         return map.put(key, value);
      }
      else
      {
         return map.remove(key);
      }
   }

   @Override
   public String toString()
   {
      return String.valueOf(map);
   }

}
