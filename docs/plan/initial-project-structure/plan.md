# Plan — ArchUnit architecture tests

## Goal
Add framework-neutral ArchUnit and JUnit 5 architecture tests to all five Java modules. The empty template must continue to pass `./gradlew clean build`, while future production classes are checked for the stable dependency and naming conventions that can be enforced without embedding the replaceable root package or inventing service-boundary heuristics.

## Scope
- In scope:
  - Centralized test-only ArchUnit 1.5.0 and JUnit 5.14.4 configuration for `core`, `service`, `client`, `consumer`, and `producer`.
  - Root-package-independent import of each module's own production class output.
  - Core contract, service-layer dependency, and exact role-package naming rules.
  - README documentation of every enforced rule, explicitly allowed directions, and the unenforced cross-boundary convention.
- Out of scope:
  - Sample, marker, fixture, or business classes in production source sets.
  - Framework-specific annotations or rules, strict Hexagonal Architecture, and new `port`, `adapter`, `application`, or `domain` packages.
  - ArchUnit duplication of Gradle module dependency direction.
  - Cross-boundary repository, mapper, or implementation rules until the template defines a machine-readable boundary identifier.

## Constraints
- The orchestrator will not push.
- The final commit will include every tracked and untracked worktree change present at finalization, including manual user changes.
- Preserve the current production dependency graph: `service`, `client`, `consumer`, and `producer` depend only on `core`; `core` has no project dependency.
- Add ArchUnit and JUnit only to test configurations; add no production library or framework.
- Architecture code and package matchers must not contain `dev.nexcraft.foo`, `foo`, `bar`, or another assumed root/boundary name.
- Every rule must use `allowEmptyShould(true)` so modules with no production classes or no matching role classes pass.
- Tests must inspect only their owning module's production output, not test classes, dependency-module output, or dependency JARs.

## Orchestration Profile
- Profile: STRICT
- Risk level: ELEVATED
- Risk signals: shared Gradle build infrastructure and test execution behavior change across five modules.
- Delegated phases:
  - Research: REQUIRED
  - Implementation: AGENT
  - Testing: AGENT
  - Review: AGENT
  - Walkthrough: AGENT
- Rationale: the change is small in code volume but affects centralized dependency resolution, every module's test task, and empty-module behavior. Sequential research, planning, implementation, testing, review, and walkthrough agents provide the required build-wide validation without expanding production scope.

## Repository Evidence
- Research input: `docs/plan/initial-project-structure/research.md`
- Relevant modules and entry points:
  - `settings.gradle.kts` — registers `core`, `service`, `client`, `consumer`, and `producer`.
  - `build.gradle.kts` and `gradle.properties` — centralize shared Java 25 configuration and are the appropriate owners for common test versions and behavior.
  - Each module's `build.gradle.kts` — applies `java-library`; the four implementation modules declare only their existing `implementation(project(":core"))` edge.
  - `*/src/main/java/dev/nexcraft/foo/` — contains only `.gitkeep` files, so architecture imports must support missing compiled-main directories.
  - `README.md` — documents the replaceable package root and currently describes ArchUnit as deferred.
- Existing contracts and public APIs:
  - Production Java API — no classes exist; architecture testing must not create a public API or sample implementation.
  - Module graph — Gradle remains the authority for module-level direction; architecture tests operate only on package/class dependencies.
- Existing test and build conventions:
  - `gradle/wrapper/gradle-wrapper.properties` — pins the Gradle 9.4.1 wrapper.
  - `./gradlew tasks --all` — identifies the standard `test` tasks for all five Java modules; no component or integration task exists.

## Standards Profile
- Selected standards:
  - `/Users/sean/.codex/skills/java-orchestrator/references/orchestrator/test-command-discovery.md`
  - `/Users/sean/.codex/skills/java-orchestrator/references/constitution/core/testing-and-quality.md`
  - `/Users/sean/.codex/skills/java-orchestrator/references/constitution/core/coding-standards.md`
- Service classification: none; this is a framework-neutral provisioning template with no runtime application.
- Non-negotiable requirements carried into this plan:
  - Keep architecture tests deterministic, isolated, local, and free of network or external-system access.
  - Use camelCase test names, lowercase packages, and English intent documentation for non-private test types and methods.
  - Keep rules readable and scoped to declared package responsibilities; do not infer roles from naming alone or add broad dependency allowlists.

## Verification Plan

### Unit scope
- Status: REQUIRED
- Working directory: `/Users/sean/Documents/projects/java-service-template`
- Command: `./gradlew clean build`
- Gradle task: `test`
- Expected JUnit XML: `*/build/test-results/test/*.xml`
- Expected HTML report: `*/build/reports/tests/test/index.html`
- Triage command: `python3 /Users/sean/.codex/skills/test-failure-triage/scripts/render_test_failure_report.py --root /Users/sean/Documents/projects/java-service-template --scope unit --task test --prefer auto`
- Discovery evidence: the five modules apply `java-library`, and `./gradlew tasks --all` lists their standard `test` tasks; the root `build` lifecycle aggregates subproject verification.

### Component or integration scope
- Status: N/A
- Working directory: N/A
- Command: N/A
- Gradle task: N/A
- Expected JUnit XML: N/A
- Expected HTML report: N/A
- Discovery evidence: no component/integration source set, task, CI workflow, or repository command exists.

## Tasks

### T01 — Centralize architecture-test infrastructure
- Context: all modules need the same test libraries and a production-only import path, while the template currently has no test configuration or compiled main directories.
- Implementation notes:
  - Add `archUnitVersion=1.5.0` and `junitVersion=5.14.4` to `gradle.properties`.
  - In the root Gradle build, configure Java subprojects with the JUnit BOM, JUnit Jupiter, the JUnit Platform launcher, and `archunit-junit5` on test-only configurations; enable `useJUnitPlatform()` for `Test` tasks.
  - Resolve the owning subproject's `main` `SourceSet` class directories, create those build-output directories immediately before its test task when an empty module produced none, and pass their path-separated value through the test JVM property `architectureTest.mainClassesDirs`.
  - Production-class import helpers must parse that property and call `ClassFileImporter.importPaths(...)` only for those directories. They must not use `importClasspath()`, `@AnalyzeClasses` package roots, or a hard-coded source/root package.
- Test notes:
  - Confirm dependency additions remain confined to test configurations and all five empty-module test tasks can initialize an empty `JavaClasses` set.
- Acceptance criteria:
  - All modules share one version/configuration source, each test JVM receives only its module's main output paths, and no production or inter-module dependency edge is added.
- Done when: Gradle configuration succeeds for all five modules and the empty production outputs are importable without skipping or failing architecture rules.
- Result:
  - Status: DONE
  - Result Notes: Added shared ArchUnit 1.5.0 and JUnit 5.14.4 test-only dependencies, Maven Central resolution for those test artifacts, JUnit Platform execution, and the module-local `architectureTest.mainClassesDirs` JVM property. Empty main output directories are prepared immediately before each test task and no classpath or dependency-module import is used.

### T02 — Enforce core service-contract conventions
- Context: `core` owns the public service interfaces, so it needs architecture coverage even though it is initially empty.
- Implementation notes:
  - Add one package-private JUnit 5 architecture test under `core/src/test/java/architecture/` that imports only `core` main output via the shared JVM-property contract.
  - For top-level classes residing in the exact role package pattern `..service`, require both interface type and a simple name ending in `Service`.
  - Apply `allowEmptyShould(true)` to the rule; do not constrain `model`, `exception`, nested contract model types, or service subpackages.
- Test notes:
  - The test must execute successfully with zero production classes and produce JUnit report evidence rather than being conditionally skipped.
- Acceptance criteria:
  - Future top-level types placed directly in a core `service` role package must be interfaces named `*Service`; unrelated core roles remain unrestricted.
- Done when: the core architecture test is discovered and passes against the empty core production output.
- Result:
  - Status: DONE
  - Result Notes: Added a package-private core architecture test that imports the owning module's production output and enforces top-level direct `service` package interfaces ending in `Service` with `allowEmptyShould(true)`.

### T03 — Enforce service dependency and naming rules
- Context: the server module contains the stable endpoint, implementation, repository, mapper, and entity responsibilities identified by the request.
- Implementation notes:
  - Add a readable JUnit 5 test under `service/src/test/java/architecture/` that imports only service main output; keep separate named rules/test methods where that makes violation output unambiguous.
  - Prohibit classes in `..endpoint..` from depending on classes in `..service.impl..`, `..repository..`, or `..mapper..`.
  - Prohibit classes in `..repository..` from depending on classes in `..endpoint..` or `..service.impl..`.
  - Prohibit classes in `..mapper..` from depending on classes in `..endpoint..`.
  - Require top-level classes in exact `..service.impl` packages to end in `ServiceImpl`, and top-level classes in exact `..repository.entity` packages to end in `Entity`.
  - Apply `allowEmptyShould(true)` independently to every dependency and naming rule. Do not add a positive allowlist: endpoint-to-contract and implementation-to-repository/mapper dependencies remain allowed by the absence of a prohibition.
- Test notes:
  - Execute every rule against the empty service output; do not add production or synthetic business fixtures solely to create matches or violations.
- Acceptance criteria:
  - All six prohibited package-dependency edges are represented explicitly, both service naming rules are scoped to top-level classes in exact role packages, and no rule restricts `config`, `filter`, or unrelated helper packages.
- Done when: all service architecture tests are discovered and pass with no production classes.
- Result:
  - Status: DONE
  - Result Notes: Added six independent service dependency prohibitions plus top-level `ServiceImpl` and `Entity` suffix rules, all scoped to the service module's imported main output and individually empty-safe.

### T04 — Enforce client and messaging naming rules
- Context: client, consumer, and producer each own one exact role package whose top-level component names have stable suffix conventions.
- Implementation notes:
  - Add one small JUnit 5 architecture test under each module's `src/test/java/architecture/`, using only that module's main output paths.
  - Require top-level classes in exact `..service.client` packages to end in `ServiceClient`, exact `..consumer` packages to end in `Consumer`, and exact `..producer` packages to end in `Producer`.
  - Apply `allowEmptyShould(true)` to every rule and leave `config` and subpackages outside the exact role-package predicates unrestricted.
- Test notes:
  - Confirm each module's test is discovered and passes with no production classes; do not introduce sample clients, consumers, or producers.
- Acceptance criteria:
  - The three suffix rules are module-local, root-package-independent, top-level-only, and empty-safe.
- Done when: client, consumer, and producer architecture tests all produce passing JUnit evidence.
- Result:
  - Status: DONE
  - Result Notes: Added module-local package-private tests for `ServiceClient`, `Consumer`, and `Producer` suffixes, each using only its owning main output and `allowEmptyShould(true)`.

### T05 — Document enforced and intentionally unenforced architecture
- Context: the README currently says ArchUnit will be added later and the user requires a concise summary of what every rule permits or prohibits.
- Implementation notes:
  - Replace the deferred ArchUnit statement with an English rule summary covering the core contract rule, all six service dependency prohibitions, and all five suffix conventions.
  - State that endpoint-to-service-contract and service-implementation-to-repository/mapper dependencies are allowed, while Gradle continues to enforce module-level direction.
  - Document cross-boundary access as an architectural convention only: direct access to another boundary's repository, mapper, or implementation is prohibited by policy, but is not yet machine-enforced because the current layout has no generic boundary identifier. Do not add `foo`/`bar` examples as executable rules, a naming heuristic, or a misleading TODO test.
  - Retain the framework-neutral, empty-template, and replaceable-root-package guidance.
- Test notes:
  - Review the README rule list against the implemented tests so each enforced rule and each intentional non-rule is accurately represented.
- Acceptance criteria:
  - Documentation no longer claims ArchUnit is deferred and clearly distinguishes enforced rules, allowed directions, Gradle-owned module constraints, and the cross-boundary limitation.
- Done when: documentation matches the final test implementation without promising unenforced behavior.
- Result:
  - Status: DONE
  - Result Notes: Replaced the deferred ArchUnit note with the complete enforced rule list, allowed dependency directions, Gradle-owned module direction, and the documented cross-boundary limitation.

## Execution Gates
- Implementation: choose **Start implementation** in the structured choice UI, or request plan revision/free-form changes.
- Review: choose the planned review, force the specialist reviewer, return to implementation, or use free-form input.
- Walkthrough: choose the planned walkthrough, force the walkthrough agent, return to review, or use free-form input.
- Final commit: choose **Approve and commit** in the structured choice UI, or request revision/free-form changes.

## Global Acceptance
- Every task acceptance criterion is met.
- `./gradlew clean build` passes from the repository root with all five modules' architecture tests discovered and JUnit XML/HTML evidence present.
- The dependency-free triage command returns usable clean unit-test evidence; exit code `2` is treated as missing evidence, not a pass.
- No production Java class, runtime dependency, framework-specific rule, root-package literal, or guessed boundary rule is introduced.
- Every required verification command passes and has report evidence.
- Component/integration testing remains recorded as `N/A` with discovery evidence, not as a pass.
- `docs/plan/initial-project-structure/review.md` has no open blocking findings from the required specialist reviewer.
- `docs/walkthrough/initial-project-structure.md` is complete and summarizes each enforced allowance, prohibition, and naming rule.

## Risks and Impact
- Centralized subproject test configuration can accidentally leak test libraries into production or import dependency classes; review must verify test-only configurations and module-owned main output paths.
- Empty Java source sets do not create main class directories under the current build, so the test task must prepare only those build-output directories before ArchUnit imports them.
- Exact role-package suffix rules intentionally treat top-level classes placed directly in those packages as role classes; helpers that do not represent the role must live outside those exact packages.
- Cross-boundary enforcement remains documentation-only until an explicit boundary convention exists; adding a heuristic now would create false positives or encode the placeholder package.

## References
- `docs/plan/initial-project-structure/research.md`
- `README.md`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.properties`
- `/Users/sean/.codex/skills/java-orchestrator/references/planner/templates.md`
- `/Users/sean/.codex/skills/java-orchestrator/references/orchestrator/test-command-discovery.md`
- `/Users/sean/.codex/skills/java-orchestrator/references/constitution/core/testing-and-quality.md`
- `/Users/sean/.codex/skills/java-orchestrator/references/constitution/core/coding-standards.md`
