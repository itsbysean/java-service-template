package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/** Defines the architecture rules for remote service implementations. */
final class ClientArchitectureRules {

    private ClientArchitectureRules() {
    }

    /** Requires direct client service implementations to use the ServiceClient suffix. */
    static ArchRule serviceClientsMustUseServiceClientSuffix() {
        return classes()
                .that()
                .resideInAnyPackage("..service.client")
                .and()
                .areTopLevelClasses()
                .should()
                .haveSimpleNameEndingWith("ServiceClient")
                .allowEmptyShould(true);
    }

    /** Requires direct client service implementation roles to be classes. */
    static ArchRule serviceClientsMustBeClasses() {
        return classes()
                .that()
                .resideInAnyPackage("..service.client")
                .and()
                .areTopLevelClasses()
                .should()
                .notBeInterfaces()
                .allowEmptyShould(true);
    }

    /** Requires client implementations to match and implement their core service contracts. */
    static ArchRule serviceClientsMustMatchCoreContracts(final JavaClasses contractClasses) {
        return classes()
                .that()
                .resideInAnyPackage("..service.client")
                .and()
                .areTopLevelClasses()
                .should(new ClientImplementationMatchesContract(contractClasses))
                .allowEmptyShould(true);
    }
}
