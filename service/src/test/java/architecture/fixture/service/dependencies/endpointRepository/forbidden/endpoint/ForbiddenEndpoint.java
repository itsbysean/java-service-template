package architecture.fixture.service.dependencies.endpointRepository.forbidden.endpoint;

import architecture.fixture.service.dependencies.endpointRepository.forbidden.repository.RepositoryDependency;

/** An endpoint fixture with a prohibited repository dependency. */
final class ForbiddenEndpoint {

    /** Holds the prohibited repository dependency. */
    private final RepositoryDependency dependency;

    /** Creates an endpoint fixture with a prohibited repository dependency. */
    ForbiddenEndpoint(final RepositoryDependency dependency) {
        this.dependency = dependency;
    }
}
