package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Proves the core architecture rules with isolated test-only fixtures. */
final class CoreArchitectureRulesTest {

    @Test
    void publicServiceInterfacePasses() {
        assertPasses(
                CoreArchitectureRules.serviceContractsMustBePublicInterfacesNamedService(),
                fixtures("architecture.fixture.core.valid.service"));
    }

    @Test
    void nonInterfaceServiceFails() {
        assertViolates(
                CoreArchitectureRules.serviceContractsMustBePublicInterfacesNamedService(),
                fixtures("architecture.fixture.core.invalidType.service"));
    }

    @Test
    void serviceWithWrongSuffixFails() {
        assertViolates(
                CoreArchitectureRules.serviceContractsMustBePublicInterfacesNamedService(),
                fixtures("architecture.fixture.core.invalidSuffix.service"));
    }

    @Test
    void nonPublicServiceInterfaceFails() {
        assertViolates(
                CoreArchitectureRules.serviceContractsMustBePublicInterfacesNamedService(),
                fixtures("architecture.fixture.core.nonPublic.service"));
    }

    @Test
    void validTopLevelAndNestedModelsPass() {
        assertPasses(
                CoreArchitectureRules.contractModelsMustNotUseDtoSuffix(),
                fixtures("architecture.fixture.core.valid.model"));
    }

    @Test
    void dtoModelFails() {
        assertViolates(
                CoreArchitectureRules.contractModelsMustNotUseDtoSuffix(),
                fixtures("architecture.fixture.core.invalidModel.model"));
    }

    private static JavaClasses fixtures(final String... packageNames) {
        return ArchitectureTestSupport.importFixturePackages(packageNames);
    }

    private static void assertPasses(final ArchRule rule, final JavaClasses classes) {
        assertDoesNotThrow(() -> rule.check(classes));
    }

    private static void assertViolates(final ArchRule rule, final JavaClasses classes) {
        assertThrows(AssertionError.class, () -> rule.check(classes));
    }
}
