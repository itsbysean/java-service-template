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

    private ArchitectureTestSupport() {
    }

    /** Imports only the production class directories supplied by the owning Gradle project. */
    static JavaClasses importMainClasses() {
        return importDirectories(MAIN_CLASSES_DIRS_PROPERTY);
    }

    /** Imports only the explicitly supplied fixture packages. */
    static JavaClasses importFixturePackages(final String... packageNames) {
        return new ClassFileImporter().importPackages(packageNames);
    }

    private static JavaClasses importDirectories(final String propertyName) {
        final String configuredDirectories = System.getProperty(propertyName);
        if (configuredDirectories == null || configuredDirectories.isBlank()) {
            throw new IllegalStateException("Missing " + propertyName + " system property");
        }

        final List<Path> classDirectories = Arrays.stream(
                        configuredDirectories.split(Pattern.quote(File.pathSeparator)))
                .filter(directory -> !directory.isBlank())
                .map(Paths::get)
                .toList();
        return new ClassFileImporter().importPaths(classDirectories);
    }
}
