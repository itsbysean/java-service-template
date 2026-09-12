package architecture.fixture.service.dependencies.endpointMapper.forbidden.endpoint;

import architecture.fixture.service.dependencies.endpointMapper.forbidden.mapper.MapperDependency;

/** An endpoint fixture with a prohibited mapper dependency. */
final class ForbiddenEndpoint {

    /** Holds the prohibited mapper dependency. */
    private final MapperDependency dependency;

    /** Creates an endpoint fixture with a prohibited mapper dependency. */
    ForbiddenEndpoint(final MapperDependency dependency) {
        this.dependency = dependency;
    }
}
