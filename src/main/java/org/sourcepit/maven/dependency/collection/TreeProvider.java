/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency.collection;

import java.util.List;

interface TreeProvider<Node>
{
   List<Node> visitChildren(Node parent, int depth, List<Node> children);

   List<Node> getChildren(Node parent);

   void leaveChildren(Node parent, int depth, List<Node> children);
}