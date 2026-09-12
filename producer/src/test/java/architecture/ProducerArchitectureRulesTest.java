package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Proves producer naming rules with isolated test-only fixtures. */
final class ProducerArchitectureRulesTest {

    @Test
    void validProducerSuffixPasses() {
        assertPasses(
                ProducerArchitectureRules.producersMustUseProducerSuffix(),
                fixtures("architecture.fixture.producer.valid.producer"));
    }

    @Test
    void invalidProducerSuffixFails() {
        assertViolates(
                ProducerArchitectureRules.producersMustUseProducerSuffix(),
                fixtures("architecture.fixture.producer.invalid.producer"));
    }

    @Test
    void unrelatedPackageIsNotSelected() {
        assertPasses(
                ProducerArchitectureRules.producersMustUseProducerSuffix(),
                fixtures("architecture.fixture.producer.outside"));
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
