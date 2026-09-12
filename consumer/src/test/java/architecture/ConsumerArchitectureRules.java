package architecture;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/** Defines the architecture rules for inbound asynchronous consumers. */
final class ConsumerArchitectureRules {

    private ConsumerArchitectureRules() {
    }

    /** Requires direct message consumers to use the Consumer suffix. */
    static ArchRule consumersMustUseConsumerSuffix() {
        return classes()
                .that()
                .resideInAnyPackage("..consumer")
                .and()
                .areTopLevelClasses()
                .should()
                .haveSimpleNameEndingWith("Consumer")
                .allowEmptyShould(true);
    }
}
