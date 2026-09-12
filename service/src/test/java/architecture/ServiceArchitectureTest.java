package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

/** Verifies service-layer dependency prohibitions and role naming conventions. */
final class ServiceArchitectureTest {

    private static final JavaClasses MAIN_CLASSES = ArchitectureTestSupport.importMainAndContractClasses();
    private static final JavaClasses CONTRACT_CLASSES = ArchitectureTestSupport.importContractClasses();

    /** Prevents endpoints from depending on service implementations. */
    @Test
    void endpointsMustNotDependOnServiceImplementations() {
        ServiceArchitectureRules.endpointsMustNotDependOnServiceImplementations().check(MAIN_CLASSES);
    }

    /** Prevents endpoints from depending on repositories. */
    @Test
    void endpointsMustNotDependOnRepositories() {
        ServiceArchitectureRules.endpointsMustNotDependOnRepositories().check(MAIN_CLASSES);
    }

    /** Prevents endpoints from depending on mappers. */
    @Test
    void endpointsMustNotDependOnMappers() {
        ServiceArchitectureRules.endpointsMustNotDependOnMappers().check(MAIN_CLASSES);
    }

    /** Prevents repositories from depending on endpoints. */
    @Test
    void repositoriesMustNotDependOnEndpoints() {
        ServiceArchitectureRules.repositoriesMustNotDependOnEndpoints().check(MAIN_CLASSES);
    }

    /** Prevents repositories from depending on service implementations. */
    @Test
    void repositoriesMustNotDependOnServiceImplementations() {
        ServiceArchitectureRules.repositoriesMustNotDependOnServiceImplementations().check(MAIN_CLASSES);
    }

    /** Prevents mappers from depending on endpoints. */
    @Test
    void mappersMustNotDependOnEndpoints() {
        ServiceArchitectureRules.mappersMustNotDependOnEndpoints().check(MAIN_CLASSES);
    }

    /** Requires direct service implementations to use the ServiceImpl suffix. */
    @Test
    void serviceImplementationsMustUseServiceImplSuffix() {
        ServiceArchitectureRules.serviceImplementationsMustUseServiceImplSuffix().check(MAIN_CLASSES);
    }

    /** Requires direct service implementation roles to be classes. */
    @Test
    void serviceImplementationsMustBeClasses() {
        ServiceArchitectureRules.serviceImplementationsMustBeClasses().check(MAIN_CLASSES);
    }

    /** Requires server implementations to match and implement their core service contracts. */
    @Test
    void serviceImplementationsMustMatchCoreContracts() {
        ServiceArchitectureRules.serviceImplementationsMustMatchCoreContracts(CONTRACT_CLASSES).check(MAIN_CLASSES);
    }

    /** Requires direct persistence entities to use the Entity suffix. */
    @Test
    void repositoryEntitiesMustUseEntitySuffix() {
        ServiceArchitectureRules.repositoryEntitiesMustUseEntitySuffix().check(MAIN_CLASSES);
    }
}
