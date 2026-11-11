package guardrails;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/** Compiles isolated bytecode fixtures and checks the specific failure reason. */
final class BoundaryRulesTest {
    @TempDir Path temporary;

    JavaClasses compile(String... sources) throws Exception {
        Path output = Files.createTempDirectory(temporary, "classes");
        var args = new ArrayList<String>();
        args.addAll(java.util.List.of("-d", output.toString()));
        for (int i = 0; i < sources.length; i += 2) {
            Path file = temporary.resolve(sources[i]);
            Files.createDirectories(file.getParent());
            Files.writeString(file, sources[i + 1]);
            args.add(file.toString());
        }
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null, args.toArray(String[]::new)));
        var classes = new ClassFileImporter().importPath(output);
        assertFalse(classes.isEmpty(), "Fixture must contain classes");
        return classes;
    }

    @Test void misplacedRoleFails() throws Exception {
        var classes = compile("UserServiceImpl.java", "package sample.other; public class UserServiceImpl {}");
        var result = BoundaryRules.boundaries("service").evaluate(classes);
        assertTrue(result.hasViolation());
        assertTrue(result.getFailureReport().toString().contains("wrong role package"));
    }

    @Test void nestedEntityNameFails() throws Exception {
        var classes = compile("Wrong.java", "package sample.repository.entity.user; public class Wrong {}");
        assertTrue(BoundaryRules.boundaries("service").evaluate(classes).getFailureReport().toString()
                .contains("entity name must end with Entity"));
    }

    @Test void coreModelCannotReferToContractThroughGenericReturn() throws Exception {
        var classes = compile("UserService.java", "package sample.service; public interface UserService {}",
                "User.java", "package sample.model; public class User { public java.util.List<sample.service.UserService> users() { return null; }}");
        assertTrue(BoundaryRules.boundaries("core").evaluate(classes).getFailureReport().toString()
                .contains("forbidden dependency"));
    }

    @Test void entityCannotExtendRepositoryBehavior() throws Exception {
        var classes = compile("Store.java", "package sample.repository; public class Store {}",
                "UserEntity.java", "package sample.repository.entity; public class UserEntity extends sample.repository.Store {}");
        assertTrue(BoundaryRules.boundaries("service").evaluate(classes).hasViolation());
    }

    @Test void mapperMayUseEntityAndModel() throws Exception {
        var classes = compile("UserEntity.java", "package sample.repository.entity; public class UserEntity {}",
                "User.java", "package sample.model; public class User {}",
                "Mapping.java", "package sample.mapper; public class Mapping { public sample.model.User map(sample.repository.entity.UserEntity entity) { return null; }}");
        assertFalse(BoundaryRules.boundaries("service").evaluate(classes).hasViolation());
    }

    @Test void mapperCannotTakeRepositoryParameter() throws Exception {
        var classes = compile("Store.java", "package sample.repository; public class Store {}",
                "Mapping.java", "package sample.mapper; public class Mapping { public void map(sample.repository.Store store) {} }");
        assertTrue(BoundaryRules.boundaries("service").evaluate(classes).hasViolation());
    }

    @Test void standardOutputFails() throws Exception {
        var classes = compile("Output.java", "package sample; public class Output { void run() { System.out.println(1); }}");
        assertTrue(BoundaryRules.standardStreams().evaluate(classes).hasViolation());
    }

    @Test void java25OutputFails() throws Exception {
        var classes = compile("Output.java", "package sample; public class Output { void run() { java.lang.IO.println(1); }}");
        assertTrue(BoundaryRules.boundaries("service").evaluate(classes).getFailureReport().toString()
                .contains("direct java.lang.IO"));
    }

    @Test void stackTraceOverloadFails() throws Exception {
        var classes = compile("Output.java", "package sample; public class Output { void run(Throwable t, java.io.PrintWriter w) { t.printStackTrace(w); }}");
        assertTrue(BoundaryRules.boundaries("service").evaluate(classes).getFailureReport().toString()
                .contains("direct stack trace"));
    }

    @Test void ordinaryCodePasses() throws Exception {
        var classes = compile("Helper.java", "package sample; public class Helper {}");
        assertFalse(BoundaryRules.boundaries("service").evaluate(classes).hasViolation());
        assertFalse(BoundaryRules.standardStreams().evaluate(classes).hasViolation());
    }

    @Test void exceptionCannotHoldServiceField() throws Exception {
        var classes = compile("UserService.java", "package sample.service; public interface UserService {}",
                "Failure.java", "package sample.exception; public class Failure extends RuntimeException { sample.service.UserService service; }");
        assertTrue(BoundaryRules.boundaries("core").evaluate(classes).hasViolation());
    }

    @Test void entityCannotReturnEndpoint() throws Exception {
        var classes = compile("Endpoint.java", "package sample.endpoint; public class Endpoint {}",
                "UserEntity.java", "package sample.repository.entity; public class UserEntity { public sample.endpoint.Endpoint get() { return null; }}");
        assertTrue(BoundaryRules.boundaries("service").evaluate(classes).hasViolation());
    }

    @Test void misplacedClientAndProducerFail() throws Exception {
        var classes = compile("UserServiceClient.java", "package sample.other; public class UserServiceClient {}",
                "EventProducer.java", "package sample.other; public class EventProducer {}");
        assertTrue(BoundaryRules.boundaries("client").evaluate(classes).hasViolation());
        assertTrue(BoundaryRules.boundaries("producer").evaluate(classes).hasViolation());
    }

    @Test void correctNestedRolesPass() throws Exception {
        var classes = compile("UserEntity.java", "package sample.repository.entity.user; public class UserEntity {}",
                "UserServiceClient.java", "package sample.service.client.user; public class UserServiceClient {}",
                "EventProducer.java", "package sample.producer.events; public class EventProducer {}");
        assertFalse(BoundaryRules.boundaries("service").evaluate(classes).hasViolation());
        assertFalse(BoundaryRules.boundaries("client").evaluate(classes).hasViolation());
        assertFalse(BoundaryRules.boundaries("producer").evaluate(classes).hasViolation());
    }
}
