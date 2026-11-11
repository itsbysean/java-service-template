package guardrails;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

/** Framework-neutral rules applied only to explicitly imported production classes. */
final class BoundaryRules {
    private BoundaryRules() {}

    static boolean role(String packageName, String role) {
        return ("." + packageName + ".").contains("." + role + ".");
    }

    static ArchRule boundaries(String module) {
        return classes().should(new ArchCondition<JavaClass>("respect role placement and dependency boundaries") {
            @Override
            public void check(JavaClass type, ConditionEvents events) {
                String pkg = type.getPackageName();
                if (type.isTopLevelClass()) {
                    String[][] roles = switch (module) {
                        case "service" -> new String[][]{{"ServiceImpl", "service.impl"}, {"Entity", "repository.entity"}};
                        case "client" -> new String[][]{{"ServiceClient", "service.client"}};
                        case "producer" -> new String[][]{{"Producer", "producer"}};
                        default -> new String[0][];
                    };
                    for (String[] entry : roles) {
                        if (type.getSimpleName().endsWith(entry[0]) && !role(pkg, entry[1])) {
                            violation(type, events, "wrong role package: " + entry[1]);
                        }
                    }
                    if (module.equals("service") && role(pkg, "repository.entity")
                            && !type.getSimpleName().endsWith("Entity")) {
                        violation(type, events, "entity name must end with Entity");
                    }
                }
                type.getDirectDependenciesFromSelf().forEach(dependency -> {
                    String target = dependency.getTargetClass().getPackageName();
                    boolean coreLeaf = module.equals("core") && (role(pkg, "model") || role(pkg, "exception"));
                    boolean entity = module.equals("service") && role(pkg, "repository.entity");
                    boolean mapper = module.equals("service") && role(pkg, "mapper");
                    boolean repositoryBehavior = role(target, "repository") && !role(target, "repository.entity");
                    if ((coreLeaf && role(target, "service"))
                            || ((entity || mapper) && (role(target, "endpoint")
                            || role(target, "service.impl") || repositoryBehavior))
                            || (entity && role(target, "mapper"))) {
                        violation(type, events, "forbidden dependency: " + dependency.getDescription());
                    }
                });
                type.getMethodCallsFromSelf().forEach(call -> {
                    if (call.getTargetOwner().getName().equals("java.lang.IO")) {
                        violation(type, events, "direct java.lang.IO access: " + call.getDescription());
                    }
                    if (call.getName().equals("printStackTrace")
                            && call.getTargetOwner().isAssignableTo(Throwable.class)) {
                        violation(type, events, "direct stack trace output: " + call.getDescription());
                    }
                });
            }
        }).allowEmptyShould(true);
    }

    static ArchRule standardStreams() {
        return NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.allowEmptyShould(true);
    }

    private static void violation(JavaClass type, ConditionEvents events, String reason) {
        events.add(SimpleConditionEvent.violated(type, type.getName() + ": " + reason));
    }
}
