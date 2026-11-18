package guardrails;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Checks the owning module output and guards against silently empty imports. */
final class ProductionGuardrailsTest {
    @Test
    void productionBoundariesAndOutput() throws Exception {
        String configured = System.getProperty("architectureTest.mainClassesDirs");
        assertNotNull(configured, "Missing main output configuration");
        var paths = Arrays.stream(configured.split(Pattern.quote(File.pathSeparator)))
                .map(Path::of).toList();
        assertFalse(paths.isEmpty());
        Set<String> expected = new HashSet<>();
        for (Path path : paths) {
            assertTrue(Files.isDirectory(path), "Invalid main output: " + path);
            try (var files = Files.walk(path)) {
                files.filter(p -> p.toString().endsWith(".class")).forEach(p -> {
                    String name = path.relativize(p).toString().replace(File.separatorChar, '.');
                    name = name.substring(0, name.length() - 6);
                    if (!name.equals("module-info")) expected.add(name);
                });
            }
        }
        Path sources = Path.of(System.getProperty("architectureTest.sources"));
        if (Files.isDirectory(sources)) {
            try (var files = Files.walk(sources)) {
                boolean hasTypes = files.anyMatch(p -> p.toString().endsWith(".java")
                        && !p.getFileName().toString().equals("package-info.java")
                        && !p.getFileName().toString().equals("module-info.java"));
                if (hasTypes) assertFalse(expected.isEmpty(), "Sources exist but main output is empty");
            }
        }
        JavaClasses classes = new ClassFileImporter().importPaths(paths);
        Set<String> actual = new HashSet<>();
        classes.forEach(type -> actual.add(type.getName()));
        assertEquals(expected, actual, "Imported classes must equal the owning main output");
        BoundaryRules.boundaries(System.getProperty("architectureTest.module")).check(classes);
        BoundaryRules.standardStreams().check(classes);
    }
}
