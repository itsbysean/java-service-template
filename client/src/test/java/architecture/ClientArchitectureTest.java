package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

/** Verifies client role naming conventions. */
final class ClientArchitectureTest {

    private static final JavaClasses MAIN_CLASSES = ArchitectureTestSupport.importMainAndContractClasses();
    private static final JavaClasses CONTRACT_CLASSES = ArchitectureTestSupport.importContractClasses();

    /** Requires direct client service implementations to use the ServiceClient suffix. */
    @Test
    void serviceClientsMustUseServiceClientSuffix() {
        ClientArchitectureRules.serviceClientsMustUseServiceClientSuffix().check(MAIN_CLASSES);
    }

    /** Requires direct client service implementation roles to be classes. */
    @Test
    void serviceClientsMustBeClasses() {
        ClientArchitectureRules.serviceClientsMustBeClasses().check(MAIN_CLASSES);
    }

    /** Requires client implementations to match and implement their core service contracts. */
    @Test
    void serviceClientsMustMatchCoreContracts() {
        ClientArchitectureRules.serviceClientsMustMatchCoreContracts(CONTRACT_CLASSES).check(MAIN_CLASSES);
    }
}
