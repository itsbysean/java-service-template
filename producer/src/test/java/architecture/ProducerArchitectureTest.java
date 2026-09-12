package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

/** Verifies producer role naming conventions. */
final class ProducerArchitectureTest {

    private static final JavaClasses MAIN_CLASSES = ArchitectureTestSupport.importMainClasses();

    /** Requires direct message producers to use the Producer suffix. */
    @Test
    void producersMustUseProducerSuffix() {
        ProducerArchitectureRules.producersMustUseProducerSuffix().check(MAIN_CLASSES);
    }
}
