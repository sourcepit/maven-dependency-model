/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.maven.dependency;

import java.util.List;

public interface TreeProvider<Node>
{
   List<Node> getRoots(List<Node> roots);

   List<Node> getChildren(Node node);
}