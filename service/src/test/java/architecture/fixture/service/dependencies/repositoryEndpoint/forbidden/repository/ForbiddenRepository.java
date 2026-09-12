package architecture.fixture.service.dependencies.repositoryEndpoint.forbidden.repository;

import architecture.fixture.service.dependencies.repositoryEndpoint.forbidden.endpoint.EndpointDependency;

/** A repository fixture with a prohibited endpoint dependency. */
final class ForbiddenRepository {

    /** Holds the prohibited endpoint dependency. */
    private final EndpointDependency dependency;

    /** Creates a repository fixture with a prohibited endpoint dependency. */
    ForbiddenRepository(final EndpointDependency dependency) {
        this.dependency = dependency;
    }
}
