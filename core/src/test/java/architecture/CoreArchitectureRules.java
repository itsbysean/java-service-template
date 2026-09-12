package architecture;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/** Defines the architecture rules for the public service-contract module. */
final class CoreArchitectureRules {

    private CoreArchitectureRules() {
    }

    /** Requires direct core service contracts to be public interfaces named with the Service suffix. */
    static ArchRule serviceContractsMustBePublicInterfacesNamedService() {
        return classes()
                .that()
                .resideInAnyPackage("..service")
                .and()
                .areTopLevelClasses()
                .should()
                .bePublic()
                .andShould()
                .beInterfaces()
                .andShould()
                .haveSimpleNameEndingWith("Service")
                .allowEmptyShould(true);
    }

    /** Prevents contract models from using the Dto suffix. */
    static ArchRule contractModelsMustNotUseDtoSuffix() {
        return classes()
                .that()
                .resideInAnyPackage("..model..")
                .should()
                .haveSimpleNameNotEndingWith("Dto")
                .allowEmptyShould(true);
    }
}
