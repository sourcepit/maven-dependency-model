/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import static org.sourcepit.common.utils.io.IO.cpIn;
import static org.sourcepit.common.utils.io.IO.read;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sourcepit.common.utils.io.IOHandle;
import org.sourcepit.common.utils.io.Read;

public final class TestDefinitionHarness
{
   private TestDefinitionHarness()
   {
      super();
   }

   public static Map<String, String> parseTestDefinition(String resourcePath) throws IOException
   {
      return splitParts(cpIn(TestDefinitionHarness.class.getClassLoader(), resourcePath));
   }

   private static final String NL = System.getProperty("line.separator");

   private static Map<String, String> splitParts(IOHandle<InputStream> res) throws IOException
   {
      return read(new Read.FromStream<Map<String, String>>()
      {
         @Override
         public Map<String, String> read(InputStream inputStream) throws Exception
         {
            Map<String, String> parts = new LinkedHashMap<String, String>();

            final BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            StringBuilder sb = null;

            String partName = null;

            for (String ln = r.readLine(); ln != null; ln = r.readLine())
            {
               if (ln.startsWith("#>>"))
               {
                  if (sb != null)
                  {
                     parts.put(partName, sb.toString());
                     partName = null;
                     sb = null;
                  }
                  partName = ln.substring(3).trim();
                  continue;
               }

               if (sb == null)
               {
                  sb = new StringBuilder();
                  sb.append(ln);
               }
               else
               {
                  sb.append(NL);
                  sb.append(ln);
               }
            }

            if (sb != null)
            {
               sb.append(NL);
               parts.put(partName, sb.toString());
               partName = null;
               sb = null;
            }

            return parts;
         }
      }, res);
   }
}
