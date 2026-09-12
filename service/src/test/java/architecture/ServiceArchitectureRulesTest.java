package architecture;

import architecture.fixture.service.match.direct.service.DirectService;
import architecture.fixture.service.match.indirect.service.InheritedService;
import architecture.fixture.service.match.interfaceRole.service.InterfaceRoleService;
import architecture.fixture.service.match.nonAssignable.service.NonAssignableService;
import architecture.fixture.service.match.nonInterface.service.NonInterfaceService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Proves server architecture rules with isolated test-only fixtures. */
final class ServiceArchitectureRulesTest {

    @Test
    void directServiceImplementationMatchesItsContract() {
        assertValidImplementation("architecture.fixture.service.match.direct.service", "architecture.fixture.service.match.direct.service.impl", DirectService.class);
    }

    @Test
    void indirectServiceImplementationMatchesItsContract() {
        assertValidImplementation("architecture.fixture.service.match.indirect.service", "architecture.fixture.service.match.indirect.service.impl", InheritedService.class);
    }

    @Test
    void missingServiceContractFails() {
        assertImplementationRelationshipFails(
                "architecture.fixture.service.match.missing.service.impl",
                "architecture.fixture.service.match.missing.service",
                "architecture.fixture.service.match.missing.service.impl");
    }

    @Test
    void nonInterfaceServiceContractFails() {
        assertImplementationRelationshipFails(
                "architecture.fixture.service.match.nonInterface.service",
                "architecture.fixture.service.match.nonInterface.service",
                "architecture.fixture.service.match.nonInterface.service.impl");
    }

    @Test
    void nonAssignableServiceImplementationFails() {
        assertImplementationRelationshipFails(
                "architecture.fixture.service.match.nonAssignable.service",
                "architecture.fixture.service.match.nonAssignable.service",
                "architecture.fixture.service.match.nonAssignable.service.impl");
    }

    @Test
    void serviceImplementationInterfaceFailsClassRule() {
        final JavaClasses classes = fixtures(
                "architecture.fixture.service.match.interfaceRole.service",
                "architecture.fixture.service.match.interfaceRole.service.impl");
        assertViolates(ServiceArchitectureRules.serviceImplementationsMustBeClasses(), classes);
        assertPasses(
                ServiceArchitectureRules.serviceImplementationsMustMatchCoreContracts(
                        contractClasses(InterfaceRoleService.class)),
                classes);
    }

    @Test
    void malformedServiceImplementationSuffixFailsWithoutThrowing() {
        final JavaClasses classes = fixtures("architecture.fixture.service.match.badSuffix.service.impl");
        assertViolates(ServiceArchitectureRules.serviceImplementationsMustUseServiceImplSuffix(), classes);
        assertViolates(
                ServiceArchitectureRules.serviceImplementationsMustMatchCoreContracts(
                        fixtures("architecture.fixture.service.match.badSuffix.service")),
                classes);
    }

    @Test
    void validEntityUsesEntitySuffix() {
        assertPasses(
                ServiceArchitectureRules.repositoryEntitiesMustUseEntitySuffix(),
                fixtures("architecture.fixture.service.entity.valid.repository.entity"));
    }

    @Test
    void invalidEntitySuffixFails() {
        assertViolates(
                ServiceArchitectureRules.repositoryEntitiesMustUseEntitySuffix(),
                fixtures("architecture.fixture.service.entity.invalid.repository.entity"));
    }

    @Test
    void endpointImplementationDependencyIsForbidden() {
        assertDependencyRule("endpointsMustNotDependOnServiceImplementations", "architecture.fixture.service.dependencies.endpointImplementation.forbidden.endpoint", "architecture.fixture.service.dependencies.endpointImplementation.forbidden.service.impl", false);
    }

    @Test
    void endpointRepositoryDependencyIsForbidden() {
        assertDependencyRule("endpointsMustNotDependOnRepositories", "architecture.fixture.service.dependencies.endpointRepository.forbidden.endpoint", "architecture.fixture.service.dependencies.endpointRepository.forbidden.repository", false);
    }

    @Test
    void endpointMapperDependencyIsForbidden() {
        assertDependencyRule("endpointsMustNotDependOnMappers", "architecture.fixture.service.dependencies.endpointMapper.forbidden.endpoint", "architecture.fixture.service.dependencies.endpointMapper.forbidden.mapper", false);
    }

    @Test
    void repositoryEndpointDependencyIsForbidden() {
        assertDependencyRule("repositoriesMustNotDependOnEndpoints", "architecture.fixture.service.dependencies.repositoryEndpoint.forbidden.repository", "architecture.fixture.service.dependencies.repositoryEndpoint.forbidden.endpoint", false);
    }

    @Test
    void repositoryImplementationDependencyIsForbidden() {
        assertDependencyRule("repositoriesMustNotDependOnServiceImplementations", "architecture.fixture.service.dependencies.repositoryImplementation.forbidden.repository", "architecture.fixture.service.dependencies.repositoryImplementation.forbidden.service.impl", false);
    }

    @Test
    void mapperEndpointDependencyIsForbidden() {
        assertDependencyRule("mappersMustNotDependOnEndpoints", "architecture.fixture.service.dependencies.mapperEndpoint.forbidden.mapper", "architecture.fixture.service.dependencies.mapperEndpoint.forbidden.endpoint", false);
    }

    @Test
    void endpointWithoutImplementationDependencyPasses() {
        assertDependencyRule("endpointsMustNotDependOnServiceImplementations", "architecture.fixture.service.dependencies.endpointImplementation.allowed.endpoint", null, true);
    }

    @Test
    void endpointWithoutRepositoryDependencyPasses() {
        assertDependencyRule("endpointsMustNotDependOnRepositories", "architecture.fixture.service.dependencies.endpointRepository.allowed.endpoint", null, true);
    }

    @Test
    void endpointWithoutMapperDependencyPasses() {
        assertDependencyRule("endpointsMustNotDependOnMappers", "architecture.fixture.service.dependencies.endpointMapper.allowed.endpoint", null, true);
    }

    @Test
    void repositoryWithoutEndpointDependencyPasses() {
        assertDependencyRule("repositoriesMustNotDependOnEndpoints", "architecture.fixture.service.dependencies.repositoryEndpoint.allowed.repository", null, true);
    }

    @Test
    void repositoryWithoutImplementationDependencyPasses() {
        assertDependencyRule("repositoriesMustNotDependOnServiceImplementations", "architecture.fixture.service.dependencies.repositoryImplementation.allowed.repository", null, true);
    }

    @Test
    void mapperWithoutEndpointDependencyPasses() {
        assertDependencyRule("mappersMustNotDependOnEndpoints", "architecture.fixture.service.dependencies.mapperEndpoint.allowed.mapper", null, true);
    }

    private static void assertValidImplementation(
            final String contractPackage,
            final String implementationPackage,
            final Class<?> contractClass) {
        final JavaClasses classes = fixtures(contractPackage, implementationPackage);
        assertPasses(ServiceArchitectureRules.serviceImplementationsMustUseServiceImplSuffix(), classes);
        assertPasses(ServiceArchitectureRules.serviceImplementationsMustBeClasses(), classes);
        assertPasses(
                ServiceArchitectureRules.serviceImplementationsMustMatchCoreContracts(contractClasses(contractClass)),
                classes);
    }

    private static void assertImplementationRelationshipFails(
            final String contractPackage,
            final String contractClassesPackage,
            final String implementationPackage) {
        final JavaClasses classes = fixtures(contractPackage, implementationPackage);
        assertPasses(ServiceArchitectureRules.serviceImplementationsMustUseServiceImplSuffix(), classes);
        assertPasses(ServiceArchitectureRules.serviceImplementationsMustBeClasses(), classes);
        assertViolates(
                ServiceArchitectureRules.serviceImplementationsMustMatchCoreContracts(
                        fixtures(contractClassesPackage)),
                classes);
    }

    private static void assertDependencyRule(
            final String ruleName,
            final String sourcePackage,
            final String targetPackage,
            final boolean expectedToPass) {
        final String allowedPackage = sourcePackage.replace("forbidden", "allowed");
        final JavaClasses classes = targetPackage == null
                ? fixtures(sourcePackage)
                : fixtures(sourcePackage, targetPackage);
        final JavaClasses allowedClasses = fixtures(allowedPackage);
        final ArchRule rule = switch (ruleName) {
            case "endpointsMustNotDependOnServiceImplementations" -> ServiceArchitectureRules.endpointsMustNotDependOnServiceImplementations();
            case "endpointsMustNotDependOnRepositories" -> ServiceArchitectureRules.endpointsMustNotDependOnRepositories();
            case "endpointsMustNotDependOnMappers" -> ServiceArchitectureRules.endpointsMustNotDependOnMappers();
            case "repositoriesMustNotDependOnEndpoints" -> ServiceArchitectureRules.repositoriesMustNotDependOnEndpoints();
            case "repositoriesMustNotDependOnServiceImplementations" -> ServiceArchitectureRules.repositoriesMustNotDependOnServiceImplementations();
            case "mappersMustNotDependOnEndpoints" -> ServiceArchitectureRules.mappersMustNotDependOnEndpoints();
            default -> throw new IllegalArgumentException("Unknown service rule: " + ruleName);
        };
        if (expectedToPass) {
            assertPasses(rule, allowedClasses);
        } else {
            assertViolates(rule, classes);
        }
    }

    private static JavaClasses fixtures(final String... packageNames) {
        return ArchitectureTestSupport.importFixturePackages(packageNames);
    }

    private static JavaClasses contractClasses(final Class<?> contractClass) {
        return ArchitectureTestSupport.importFixtureClasses(contractClass);
    }

    private static void assertPasses(final ArchRule rule, final JavaClasses classes) {
        assertDoesNotThrow(() -> rule.check(classes));
    }

    private static void assertViolates(final ArchRule rule, final JavaClasses classes) {
        assertThrows(AssertionError.class, () -> rule.check(classes));
    }
}
