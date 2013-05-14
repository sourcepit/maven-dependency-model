/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.model;
public class ArtifactAttachment
{
   private final String classifier, type;

   private final boolean required;

   public ArtifactAttachment(String classifier, String type, boolean required)
   {
      this.classifier = classifier == null ? "" : classifier;
      this.type = type;
      this.required = required;
   }

   public String getClassifier()
   {
      return classifier;
   }

   public String getType()
   {
      return type;
   }

   public boolean isRequired()
   {
      return required;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
      result = prime * result + (required ? 1231 : 1237);
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      ArtifactAttachment other = (ArtifactAttachment) obj;
      if (classifier == null)
      {
         if (other.classifier != null)
         {
            return false;
         }
      }
      else if (!classifier.equals(other.classifier))
      {
         return false;
      }
      if (required != other.required)
      {
         return false;
      }
      if (type == null)
      {
         if (other.type != null)
         {
            return false;
         }
      }
      else if (!type.equals(other.type))
      {
         return false;
      }
      return true;
   }
}