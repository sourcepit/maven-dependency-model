
package org.sourcepit.maven.dependency;

public interface CustomModelAdapter<Model>
{
   Model adapt(DependencyNode node);
}
