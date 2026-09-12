package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Defines the architecture rules for the server-side service module. */
final class ServiceArchitectureRules {

    private ServiceArchitectureRules() {
    }

    /** Prevents endpoints from depending on service implementations. */
    static ArchRule endpointsMustNotDependOnServiceImplementations() {
        return noClasses()
                .that()
                .resideInAnyPackage("..endpoint..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..service.impl..")
                .allowEmptyShould(true);
    }

    /** Prevents endpoints from depending on repositories. */
    static ArchRule endpointsMustNotDependOnRepositories() {
        return noClasses()
                .that()
                .resideInAnyPackage("..endpoint..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..repository..")
                .allowEmptyShould(true);
    }

    /** Prevents endpoints from depending on mappers. */
    static ArchRule endpointsMustNotDependOnMappers() {
        return noClasses()
                .that()
                .resideInAnyPackage("..endpoint..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..mapper..")
                .allowEmptyShould(true);
    }

    /** Prevents repositories from depending on endpoints. */
    static ArchRule repositoriesMustNotDependOnEndpoints() {
        return noClasses()
                .that()
                .resideInAnyPackage("..repository..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..endpoint..")
                .allowEmptyShould(true);
    }

    /** Prevents repositories from depending on service implementations. */
    static ArchRule repositoriesMustNotDependOnServiceImplementations() {
        return noClasses()
                .that()
                .resideInAnyPackage("..repository..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..service.impl..")
                .allowEmptyShould(true);
    }

    /** Prevents mappers from depending on endpoints. */
    static ArchRule mappersMustNotDependOnEndpoints() {
        return noClasses()
                .that()
                .resideInAnyPackage("..mapper..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..endpoint..")
                .allowEmptyShould(true);
    }

    /** Requires direct service implementations to use the ServiceImpl suffix. */
    static ArchRule serviceImplementationsMustUseServiceImplSuffix() {
        return classes()
                .that()
                .resideInAnyPackage("..service.impl")
                .and()
                .areTopLevelClasses()
                .should()
                .haveSimpleNameEndingWith("ServiceImpl")
                .allowEmptyShould(true);
    }

    /** Requires direct service implementation roles to be classes. */
    static ArchRule serviceImplementationsMustBeClasses() {
        return classes()
                .that()
                .resideInAnyPackage("..service.impl")
                .and()
                .areTopLevelClasses()
                .should()
                .notBeInterfaces()
                .allowEmptyShould(true);
    }

    /** Requires server implementations to match and implement their core service contracts. */
    static ArchRule serviceImplementationsMustMatchCoreContracts(final JavaClasses contractClasses) {
        return classes()
                .that()
                .resideInAnyPackage("..service.impl")
                .and()
                .areTopLevelClasses()
                .should(new ServiceImplementationMatchesContract(contractClasses))
                .allowEmptyShould(true);
    }

    /** Requires direct persistence entities to use the Entity suffix. */
    static ArchRule repositoryEntitiesMustUseEntitySuffix() {
        return classes()
                .that()
                .resideInAnyPackage("..repository.entity")
                .and()
                .areTopLevelClasses()
                .should()
                .haveSimpleNameEndingWith("Entity")
                .allowEmptyShould(true);
    }
}
