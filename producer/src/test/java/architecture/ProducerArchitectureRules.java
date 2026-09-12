package architecture;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/** Defines the architecture rules for outbound asynchronous producers. */
final class ProducerArchitectureRules {

    private ProducerArchitectureRules() {
    }

    /** Requires direct message producers to use the Producer suffix. */
    static ArchRule producersMustUseProducerSuffix() {
        return classes()
                .that()
                .resideInAnyPackage("..producer")
                .and()
                .areTopLevelClasses()
                .should()
                .haveSimpleNameEndingWith("Producer")
                .allowEmptyShould(true);
    }
}
