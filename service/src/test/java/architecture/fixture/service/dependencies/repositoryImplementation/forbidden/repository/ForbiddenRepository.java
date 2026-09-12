package architecture.fixture.service.dependencies.repositoryImplementation.forbidden.repository;

import architecture.fixture.service.dependencies.repositoryImplementation.forbidden.service.impl.ImplementationDependency;

/** A repository fixture with a prohibited implementation dependency. */
final class ForbiddenRepository {

    /** Holds the prohibited implementation dependency. */
    private final ImplementationDependency dependency;

    /** Creates a repository fixture with a prohibited implementation dependency. */
    ForbiddenRepository(final ImplementationDependency dependency) {
        this.dependency = dependency;
    }
}
