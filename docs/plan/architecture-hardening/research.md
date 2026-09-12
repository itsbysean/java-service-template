# Research — ArchUnit Architecture Hardening

## Request

Strengthen the existing framework-neutral ArchUnit tests for the empty five-module Java template. Investigate generic core-contract implementation matching, logger policy, and additional stable architecture rules without assuming a root package, business boundary naming scheme, or framework.

## Research Mode

- Source: SINGLE_AGENT
- Additional model calls: 1, required for the bounded architecture-rule investigation

## Repository Snapshot

- Repository root: `/Users/sean/Documents/projects/java-service-template`
- Current branch: `feat/initial-project-structure`
- Worktree state: initial template and architecture-test changes are uncommitted; `.idea/` and build outputs are ignored.
- Relevant modules: `core` (public contracts), `service` (server implementation), `client` (remote contracts), `consumer` (inbound messaging), and `producer` (outbound messaging).

## Scope Map

| Area | Entry point or owner | Why it matters | Evidence |
|---|---|---|---|
| Shared ArchUnit setup | `build.gradle.kts`, `gradle.properties` | Controls test-only dependencies and imported class directories | Root build configuration |
| Core rules | `core/src/test/java/architecture/CoreArchitectureTest.java` | Defines contract naming/type constraints | Existing core test |
| Service rules | `service/src/test/java/architecture/ServiceArchitectureTest.java` | Defines dependency prohibitions and server naming | Existing service test |
| Client rules | `client/src/test/java/architecture/ClientArchitectureTest.java` | Defines remote implementation naming | Existing client test |
| Messaging rules | Consumer/producer architecture tests | Defines messaging naming | Existing module tests |
| Import boundary | `*/src/test/java/architecture/ArchitectureTestSupport.java` | Currently imports only owning module output | Existing support helper |

## Execution and Data Flow

1. Each module's Gradle `test` task receives `architectureTest.mainClassesDirs` from the root build.
2. `ArchitectureTestSupport#importMainClasses()` calls `ClassFileImporter#importPaths(...)` only for that module's production output.
3. Architecture rules run against the resulting `JavaClasses`.
4. `service` and `client` cannot currently verify assignability to core contracts because core output is not imported.
5. Gradle project dependencies continue to provide the production graph: the four implementation modules depend only on `core`.

## Public Contracts and Compatibility

| Contract | Current behavior | Constraint | Evidence |
|---|---|---|---|
| Core service contracts | Direct `..service` top-level types must be interfaces ending in `Service` | Preserve replaceable root and add no production classes | `CoreArchitectureTest` |
| Server implementations | Direct `..service.impl` types are suffix-checked only | Add generic contract relationship checks | `ServiceArchitectureTest` |
| Client implementations | Direct `..service.client` types are suffix-checked only | Match corresponding core interface generically | `ClientArchitectureTest` |
| Module direction | Gradle owns project dependency direction | Do not duplicate it in ArchUnit | Module build files |
| Logger policy | No logger API is selected | Avoid SLF4J, Log4j, Spring, Jakarta, or guessed logger-name rules | Framework-neutral requirements |

## ArchUnit Feasibility

- `ClassFileImporter#importPaths(Collection<Path>)` supports importing multiple explicitly selected output directories, so service/client tests can import their own output plus core output.
- `JavaClass#getAllClassesSelfIsAssignableTo()` includes the class itself, superclasses, and implemented interfaces, making indirect implementation checks feasible.
- `getPackageName()`, `getSimpleName()`, `isInterface()`, and `isTopLevelClass()` support root-independent custom conditions.
- A small custom `ArchCondition<JavaClass>` can derive the expected contract FQCN from the implementation package/name and verify existence, interface type, and assignability.
- ArchUnit 1.5.0 supports the required import, inheritance, and custom-condition APIs.

## Recommended Contract Rule

- For a server class in `X.service.impl`, remove `.impl` to derive contract package `X.service`.
- For a client class in `X.service.client`, remove `.client` to derive contract package `X.service`.
- Remove `Impl` or `Client` from the implementation simple name to derive `FooService`.
- Require that derived class to exist in imported core output, be an interface, and be assignable from the implementation.
- Use `getAllClassesSelfIsAssignableTo()` so an implementation may inherit from an intermediate base class that implements the contract.
- Keep rules empty-safe, but treat a missing contract as a violation once a role class exists because it indicates a naming or placement error.

Additional Gradle wiring is required: pass a second `architectureTest.contractClassesDirs` property for service/client, resolved from `project(":core")`'s main output. The helper imports the union of owning and contract output while core/consumer/producer remain module-local.

## Logger Candidate Matrix

| Candidate | Stability | False-positive risk | Recommendation |
|---|---:|---:|---|
| Require a logger field in every implementation/endpoint/repository | Low | High | Exclude |
| Match fields whose simple type is `Logger` | Low | High | Exclude |
| Forbid SLF4J, Log4j, Spring, or Jakarta logging | Low for this template | High | Exclude |
| Forbid only `java.util.logging.Logger` in core | Medium | Medium | Defer unless JUL is adopted |
| Document core logging-free policy; choose implementation logging later | High | Low | Adopt now |

## Candidate Additional Rules

| Rule | Assessment | Recommendation |
|---|---|---|
| Core service contracts are public interfaces | Stable and low-risk | Enforce `bePublic()` |
| `service.impl` and `service.client` role types are concrete | Stable and low-risk | Enforce `notBeInterfaces()` |
| Contract/implementation naming and assignability | Directly requested and testable | Enforce with custom condition |
| Existing service dependency prohibitions | Stable and explicit | Retain |
| Existing role suffix conventions | Stable when exact-package scoped | Retain |
| Package cycles across arbitrary roles | Possible but may over-constrain helper packages | Defer/document |
| Global package leaks or broad allowlists | Likely to reject legitimate helpers/framework types | Exclude |
| Visibility, constructor-injection, and field-finality rules | No explicit architecture policy | Document only |
| Cross-business-boundary repository/mapper access | No machine-readable boundary identifier | Keep documentation-only |
| Gradle module dependency direction | Already enforced by Gradle | Do not duplicate |

## Test Strategy

The current suite proves empty-safe execution but not positive/negative behavior. Add test-only fixture classes under architecture-test fixture packages, never production source sets, and import fixture output separately from production output. Cover valid and invalid contract matches, indirect implementation, invalid core contract type/name, existing dependency prohibitions, and invalid role suffixes. Keep fixtures isolated so they cannot satisfy production architecture imports.

## Build and Test Evidence

### Unit scope

- Status: REQUIRED
- Working directory: `/Users/sean/Documents/projects/java-service-template`
- Command: `./gradlew clean build`
- Expected JUnit XML: `*/build/test-results/test/*.xml`
- Expected HTML report: `*/build/reports/tests/test/index.html`
- Discovery evidence: all five modules expose standard `test` tasks; the prior run produced five XML reports with 12 passing tests.

### Component or integration scope

- Status: N/A
- Working directory: N/A
- Command: N/A
- Expected reports: N/A
- Discovery evidence: no component/integration source set or task exists.

## Candidate Impact Areas

| Area | Likely files or symbols | Evidence-backed reason |
|---|---|---|
| Contract import wiring | `build.gradle.kts`, module support helpers | Service/client need core output in addition to own output |
| Contract rules | Service/client architecture tests | Requested `FooServiceImpl`/`FooServiceClient` relationship |
| Fixture tests | Module test source trees | Current empty suite lacks violation behavior evidence |
| Core visibility rule | Core architecture test | Public contracts cross module boundaries |
| Documentation | `README.md` | Must state logger policy and added rules |

## Uncertainties and User Decisions

- Resolved by evidence:
  - Generic contract matching and indirect assignability are feasible.
  - Service/client must import core output for relationship checks.
  - Logger field enforcement would guess an API and create false positives.
  - Package cycle, global leak, and visibility rules lack a stable policy or boundary identifier.
- Requires user decision:
  - None for the recommended framework-neutral defaults: document logging policy, enforce contract relationships, and add isolated test fixtures.
- Remaining uncertainty:
  - The planner should choose the smallest fixture layout that demonstrates rule behavior without entering production output.

## Evidence Index

- `build.gradle.kts` — centralized ArchUnit dependency and module-local output property.
- Existing module architecture tests — current rules and empty-safe behavior.
- `*/src/test/java/architecture/ArchitectureTestSupport.java` — current production-only importer.
- `README.md` — replaceable root and architecture documentation.
- ArchUnit 1.5.0 User Guide — package, inheritance, cycles, custom conditions, and general coding rule capabilities: https://www.archunit.org/userguide/html/000_Index.html
