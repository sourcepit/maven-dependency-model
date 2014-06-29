/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.impl;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
import org.sourcepit.maven.dependency.VersionChooser;

public class HighestVersionChooser implements VersionChooser
{
   @Override
   public Version chooseVersion(VersionRangeResult versionRangeResult)
   {
      return getHighestVersion(versionRangeResult.getVersions());
   }

   private static Version getHighestVersion(Collection<Version> versions)
   {
      if (versions.isEmpty())
      {
         return null;
      }
      final Iterator<Version> it = versions.iterator();
      Version max = it.next();
      while (it.hasNext())
      {
         final Version version = (Version) it.next();
         if (max.compareTo(version) < 0)
         {
            max = version;
         }
      }
      return max;
   }
}
