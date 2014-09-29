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

package org.sourcepit.maven.dependency;

public interface ConflictKeyAdapter<Key>
{
   Key getConflictKey(DependencyNode node);

   Key mergeConflictKeys(Key key1, Key key2);

   Key mergeConflictKeys(DependencyNode node, Key key);

   boolean conflicts(Key key1, Key key2);

   boolean conflicts(DependencyNode node1, DependencyNode node2);
}
