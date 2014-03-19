/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model.poc;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractGraphParser
{
   public void parse(String in)
   {
      try
      {
         parse(new ByteArrayInputStream(in.getBytes("UTF-8")));
      }
      catch (IOException e)
      {
         throw pipe(e);
      }
   }

   public void parse(InputStream in) throws IOException
   {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      String line = reader.readLine();
      while (line != null)
      {
         parseLine(line);
         line = reader.readLine();
      }
   }

   private void parseLine(String line)
   {
      final char[] chars = line.toCharArray();

      final int startIdx = getStartIdx(chars);
      if (startIdx > -1)
      {
         // groupId:artifactId:ext:version:scope:optional

         int length = chars.length - startIdx;
         for (int i = startIdx; i < chars.length; i++)
         {
            switch (chars[i])
            {
               case ' ' :
                  length = i;
               default :
                  break;
            }
         }

         final String dependency = new String(chars, startIdx, length);
         visit(startIdx, dependency.split(":"));
      }
   }

   protected abstract void visit(int depth, String[] dependency);

   private int getStartIdx(char[] chars)
   {
      for (int i = 0; i < chars.length; i++)
      {
         switch (chars[i])
         {
            case '#' :
               return -1;
            case ' ' :
            case '|' :
            case '+' :
            case '*' :
            case '\\' :
            case '-' :
               break;
            default :
               return i;
         }
      }

      return -1;
   }
}
