package org.sourcepit.maven.dependency.collection;

import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.graph.Dependency;

/* * @see DefaultDependencyCollector
 */
class DefaultDependencyCollectionContext
    implements DependencyCollectionContext
{

    private RepositorySystemSession session;

    private Dependency dependency;

    private List<Dependency> managedDependencies;

    public DefaultDependencyCollectionContext( RepositorySystemSession session, Dependency dependency,
                                               List<Dependency> managedDependencies )
    {
        this.session = session;
        this.dependency = dependency;
        this.managedDependencies = managedDependencies;
    }

    public RepositorySystemSession getSession()
    {
        return session;
    }

    public Dependency getDependency()
    {
        return dependency;
    }

    public List<Dependency> getManagedDependencies()
    {
        return managedDependencies;
    }

    public void set( Dependency dependency, List<Dependency> managedDependencies )
    {
        this.dependency = dependency;
        this.managedDependencies = managedDependencies;
    }

    @Override
    public String toString()
    {
        return String.valueOf( getDependency() );
    }

}