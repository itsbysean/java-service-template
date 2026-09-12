package architecture.fixture.service.dependencies.endpointImplementation.forbidden.endpoint;

import architecture.fixture.service.dependencies.endpointImplementation.forbidden.service.impl.ImplementationDependency;

/** An endpoint fixture with a prohibited implementation dependency. */
final class ForbiddenEndpoint {

    /** Holds the prohibited implementation dependency. */
    private final ImplementationDependency dependency;

    /** Creates an endpoint fixture with a prohibited implementation dependency. */
    ForbiddenEndpoint(final ImplementationDependency dependency) {
        this.dependency = dependency;
    }
}
