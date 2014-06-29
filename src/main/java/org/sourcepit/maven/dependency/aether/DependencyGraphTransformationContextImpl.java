
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
