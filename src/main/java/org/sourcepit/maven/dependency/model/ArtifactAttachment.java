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