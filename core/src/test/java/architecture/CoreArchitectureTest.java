package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

/** Verifies the architecture of the public service-contract module. */
final class CoreArchitectureTest {

    private static final JavaClasses MAIN_CLASSES = ArchitectureTestSupport.importMainClasses();

    /** Requires direct core service contracts to be interfaces named with the Service suffix. */
    @Test
    void serviceContractsMustBeInterfacesNamedService() {
        CoreArchitectureRules.serviceContractsMustBePublicInterfacesNamedService().check(MAIN_CLASSES);
    }

    /** Prevents contract models from using the Dto suffix. */
    @Test
    void contractModelsMustNotUseDtoSuffix() {
        CoreArchitectureRules.contractModelsMustNotUseDtoSuffix().check(MAIN_CLASSES);
    }
}
