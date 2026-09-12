package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/** Provides the module-owned production output used by architecture tests. */
final class ArchitectureTestSupport {

    private static final String MAIN_CLASSES_DIRS_PROPERTY = "architectureTest.mainClassesDirs";
    private static final String CONTRACT_CLASSES_DIRS_PROPERTY = "architectureTest.contractClassesDirs";

    private ArchitectureTestSupport() {
    }

    /** Imports only the production class directories supplied by the owning Gradle project. */
    static JavaClasses importMainClasses() {
        return importDirectories(MAIN_CLASSES_DIRS_PROPERTY);
    }

    /** Imports only the explicitly configured core contract output. */
    static JavaClasses importContractClasses() {
        return importDirectories(CONTRACT_CLASSES_DIRS_PROPERTY);
    }

    /** Imports the owning module output and the explicitly configured core contract output together. */
    static JavaClasses importMainAndContractClasses() {
        final List<Path> mainDirectories = directoriesFrom(MAIN_CLASSES_DIRS_PROPERTY);
        final List<Path> contractDirectories = directoriesFrom(CONTRACT_CLASSES_DIRS_PROPERTY);
        final List<Path> allDirectories = new java.util.ArrayList<>(mainDirectories);
        allDirectories.addAll(contractDirectories);
        return new ClassFileImporter().importPaths(allDirectories);
    }

    /** Imports only the explicitly supplied fixture packages. */
    static JavaClasses importFixturePackages(final String... packageNames) {
        return new ClassFileImporter().importPackages(packageNames);
    }

    /** Imports only the explicitly supplied fixture classes. */
    static JavaClasses importFixtureClasses(final Class<?>... classes) {
        return new ClassFileImporter().importClasses(classes);
    }

    private static JavaClasses importDirectories(final String propertyName) {
        return new ClassFileImporter().importPaths(directoriesFrom(propertyName));
    }

    private static List<Path> directoriesFrom(final String propertyName) {
        final String configuredDirectories = System.getProperty(propertyName);
        if (configuredDirectories == null || configuredDirectories.isBlank()) {
            throw new IllegalStateException("Missing " + propertyName + " system property");
        }

        return Arrays.stream(configuredDirectories.split(Pattern.quote(File.pathSeparator)))
                .filter(directory -> !directory.isBlank())
                .map(Paths::get)
                .toList();
    }
}
