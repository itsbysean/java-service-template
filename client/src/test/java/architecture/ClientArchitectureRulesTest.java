package architecture;

import architecture.fixture.client.match.direct.service.DirectService;
import architecture.fixture.client.match.indirect.service.InheritedService;
import architecture.fixture.client.match.interfaceRole.service.InterfaceRoleService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Proves client architecture rules with isolated test-only fixtures. */
final class ClientArchitectureRulesTest {

    @Test
    void directClientImplementationMatchesItsContract() {
        assertValidImplementation("architecture.fixture.client.match.direct.service", "architecture.fixture.client.match.direct.service.client", DirectService.class);
    }

    @Test
    void indirectClientImplementationMatchesItsContract() {
        assertValidImplementation("architecture.fixture.client.match.indirect.service", "architecture.fixture.client.match.indirect.service.client", InheritedService.class);
    }

    @Test
    void missingClientContractFails() {
        assertImplementationRelationshipFails(
                "architecture.fixture.client.match.missing.service",
                "architecture.fixture.client.match.missing.service.client",
                "architecture.fixture.client.match.missing.service");
    }

    @Test
    void nonInterfaceClientContractFails() {
        assertImplementationRelationshipFails(
                "architecture.fixture.client.match.nonInterface.service",
                "architecture.fixture.client.match.nonInterface.service.client",
                "architecture.fixture.client.match.nonInterface.service");
    }

    @Test
    void nonAssignableClientImplementationFails() {
        assertImplementationRelationshipFails(
                "architecture.fixture.client.match.nonAssignable.service",
                "architecture.fixture.client.match.nonAssignable.service.client",
                "architecture.fixture.client.match.nonAssignable.service");
    }

    @Test
    void clientImplementationInterfaceFailsClassRule() {
        final JavaClasses classes = fixtures(
                "architecture.fixture.client.match.interfaceRole.service",
                "architecture.fixture.client.match.interfaceRole.service.client");
        assertViolates(ClientArchitectureRules.serviceClientsMustBeClasses(), classes);
        assertPasses(
                ClientArchitectureRules.serviceClientsMustMatchCoreContracts(
                        contractClasses(InterfaceRoleService.class)),
                classes);
    }

    @Test
    void malformedClientImplementationSuffixFailsWithoutThrowing() {
        final JavaClasses classes = fixtures("architecture.fixture.client.match.badSuffix.service.client");
        assertViolates(ClientArchitectureRules.serviceClientsMustUseServiceClientSuffix(), classes);
        assertViolates(
                ClientArchitectureRules.serviceClientsMustMatchCoreContracts(
                        fixtures("architecture.fixture.client.match.badSuffix.service")),
                classes);
    }

    private static void assertValidImplementation(
            final String contractPackage,
            final String implementationPackage,
            final Class<?> contractClass) {
        final JavaClasses classes = fixtures(contractPackage, implementationPackage);
        assertPasses(ClientArchitectureRules.serviceClientsMustUseServiceClientSuffix(), classes);
        assertPasses(ClientArchitectureRules.serviceClientsMustBeClasses(), classes);
        assertPasses(
                ClientArchitectureRules.serviceClientsMustMatchCoreContracts(contractClasses(contractClass)),
                classes);
    }

    private static void assertImplementationRelationshipFails(
            final String contractPackage,
            final String implementationPackage,
            final String contractClassesPackage) {
        final JavaClasses classes = fixtures(contractPackage, implementationPackage);
        assertPasses(ClientArchitectureRules.serviceClientsMustUseServiceClientSuffix(), classes);
        assertPasses(ClientArchitectureRules.serviceClientsMustBeClasses(), classes);
        assertViolates(
                ClientArchitectureRules.serviceClientsMustMatchCoreContracts(
                        fixtures(contractClassesPackage)),
                classes);
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
