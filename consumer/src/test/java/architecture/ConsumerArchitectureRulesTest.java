package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Proves consumer naming rules with isolated test-only fixtures. */
final class ConsumerArchitectureRulesTest {

    @Test
    void validConsumerSuffixPasses() {
        assertPasses(
                ConsumerArchitectureRules.consumersMustUseConsumerSuffix(),
                fixtures("architecture.fixture.consumer.valid.consumer"));
    }

    @Test
    void invalidConsumerSuffixFails() {
        assertViolates(
                ConsumerArchitectureRules.consumersMustUseConsumerSuffix(),
                fixtures("architecture.fixture.consumer.invalid.consumer"));
    }

    @Test
    void unrelatedPackageIsNotSelected() {
        assertPasses(
                ConsumerArchitectureRules.consumersMustUseConsumerSuffix(),
                fixtures("architecture.fixture.consumer.outside"));
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
