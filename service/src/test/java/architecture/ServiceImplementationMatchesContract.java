package architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** Checks the name-derived core contract of a server implementation. */
final class ServiceImplementationMatchesContract extends ArchCondition<JavaClass> {

    private static final String IMPLEMENTATION_PACKAGE_SUFFIX = ".impl";
    private static final String IMPLEMENTATION_NAME_SUFFIX = "Impl";
    private static final String SERVICE_NAME_SUFFIX = "ServiceImpl";

    private final JavaClasses contractClasses;

    ServiceImplementationMatchesContract(final JavaClasses contractClasses) {
        super("implement their derived core service contract");
        this.contractClasses = contractClasses;
    }

    @Override
    public void check(final JavaClass implementation, final ConditionEvents events) {
        final String implementationName = implementation.getName();
        final String simpleName = implementation.getSimpleName();
        if (!simpleName.endsWith(SERVICE_NAME_SUFFIX)) {
            events.add(SimpleConditionEvent.violated(
                    implementation,
                    implementationName + " does not have a valid ServiceImpl suffix, so its core contract cannot be derived"));
            return;
        }

        final String packageName = implementation.getPackageName();
        if (!packageName.endsWith(IMPLEMENTATION_PACKAGE_SUFFIX)) {
            events.add(SimpleConditionEvent.violated(
                    implementation,
                    implementationName + " is not in a direct service.impl package, so its core contract cannot be derived"));
            return;
        }

        final String contractPackage = packageName.substring(
                0,
                packageName.length() - IMPLEMENTATION_PACKAGE_SUFFIX.length());
        final String contractSimpleName = simpleName.substring(
                0,
                simpleName.length() - IMPLEMENTATION_NAME_SUFFIX.length());
        final String contractName = contractPackage + "." + contractSimpleName;

        if (!contractClasses.contain(contractName)) {
            events.add(SimpleConditionEvent.violated(
                    implementation,
                    implementationName + " has no core service contract named " + contractName));
            return;
        }

        final JavaClass contract = contractClasses.get(contractName);
        if (!contract.isInterface() || !contract.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC)) {
            events.add(SimpleConditionEvent.violated(
                    implementation,
                    implementationName + " derives " + contractName + ", but that core contract is not a public interface"));
            return;
        }

        final boolean assignableToContract = implementation.getAllClassesSelfIsAssignableTo().stream()
                .anyMatch(assignableType -> assignableType.getName().equals(contractName));
        if (!assignableToContract) {
            events.add(SimpleConditionEvent.violated(
                    implementation,
                    implementationName + " is not assignable to its core service contract " + contractName));
        }
    }
}
