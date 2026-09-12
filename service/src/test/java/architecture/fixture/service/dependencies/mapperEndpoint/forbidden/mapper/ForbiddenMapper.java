package architecture.fixture.service.dependencies.mapperEndpoint.forbidden.mapper;

import architecture.fixture.service.dependencies.mapperEndpoint.forbidden.endpoint.EndpointDependency;

/** A mapper fixture with a prohibited endpoint dependency. */
final class ForbiddenMapper {

    /** Holds the prohibited endpoint dependency. */
    private final EndpointDependency dependency;

    /** Creates a mapper fixture with a prohibited endpoint dependency. */
    ForbiddenMapper(final EndpointDependency dependency) {
        this.dependency = dependency;
    }
}
