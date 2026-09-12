package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

/** Verifies consumer role naming conventions. */
final class ConsumerArchitectureTest {

    private static final JavaClasses MAIN_CLASSES = ArchitectureTestSupport.importMainClasses();

    /** Requires direct message consumers to use the Consumer suffix. */
    @Test
    void consumersMustUseConsumerSuffix() {
        ConsumerArchitectureRules.consumersMustUseConsumerSuffix().check(MAIN_CLASSES);
    }
}
