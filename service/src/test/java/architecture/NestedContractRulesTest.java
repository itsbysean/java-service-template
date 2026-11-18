package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises real nested bytecode with an explicitly separate contract output. */
final class NestedContractRulesTest {
    @TempDir Path temporary;

    Path compile(Path output, String... sources) throws Exception {
        Files.createDirectories(output);
        var args = new ArrayList<String>(java.util.List.of("-d", output.toString(),
                "-classpath", temporary.resolve("contracts").toString()));
        for (int i = 0; i < sources.length; i += 2) {
            Path file = temporary.resolve("sources").resolve(sources[i]);
            Files.createDirectories(file.getParent());
            Files.writeString(file, sources[i + 1]);
            args.add(file.toString());
        }
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null, args.toArray(String[]::new)));
        return output;
    }

    JavaClasses contracts() throws Exception {
        return new ClassFileImporter().importPath(compile(temporary.resolve("contracts"),
                "a/UserService.java", "package a.service.user; public interface UserService {}",
                "b/UserService.java", "package b.service.user; public interface UserService {}"));
    }

    @Test void nestedImplementationMatchesRelativeContract() throws Exception {
        JavaClasses contracts = contracts();
        Path own = compile(temporary.resolve("own"), "UserServiceImpl.java",
                "package a.service.impl.user; public class UserServiceImpl implements a.service.user.UserService {}");
        JavaClasses classes = new ClassFileImporter().importPaths(own, temporary.resolve("contracts"));
        assertFalse(classes.isEmpty());
        assertFalse(ServiceArchitectureRules.serviceImplementationsMustMatchCoreContracts(contracts).evaluate(classes).hasViolation());
    }

    @Test void sameSimpleNameInAnotherRootDoesNotMatch() throws Exception {
        JavaClasses contracts = contracts();
        Path own = compile(temporary.resolve("own"), "UserServiceImpl.java",
                "package a.service.impl.user; public class UserServiceImpl implements b.service.user.UserService {}");
        var result = ServiceArchitectureRules.serviceImplementationsMustMatchCoreContracts(contracts)
                .evaluate(new ClassFileImporter().importPaths(own, temporary.resolve("contracts")));
        assertTrue(result.hasViolation());
        assertTrue(result.getFailureReport().toString().contains("not assignable"));
    }

    @Test void contractMustComeFromExplicitCoreOutput() throws Exception {
        contracts();
        Path own = compile(temporary.resolve("own"), "UserServiceImpl.java",
                "package a.service.impl.user; public class UserServiceImpl implements a.service.user.UserService {}");
        JavaClasses emptyCore = new ClassFileImporter().importPath(Files.createDirectory(temporary.resolve("empty")));
        var result = ServiceArchitectureRules.serviceImplementationsMustMatchCoreContracts(emptyCore)
                .evaluate(new ClassFileImporter().importPaths(own, temporary.resolve("contracts")));
        assertTrue(result.hasViolation());
        assertTrue(result.getFailureReport().toString().contains("has no core service contract"));
    }
}
