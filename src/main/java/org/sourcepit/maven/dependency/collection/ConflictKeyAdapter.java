/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

public interface ConflictKeyAdapter<Key>
{
   Key getConflictKey(DependencyResolutionNode node);

   Key mergeConflictKeys(Key key1, Key key2);
   
   Key mergeConflictKeys(DependencyResolutionNode node, Key key);

   boolean conflicts(Key key1, Key key2);

   boolean conflicts(DependencyResolutionNode node1, DependencyResolutionNode node2);
}
