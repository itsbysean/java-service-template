# Plan — ArchUnit architecture hardening

## Goal
Strengthen the template's ArchUnit suite so server and remote implementations are proven to match and implement their corresponding `core` service contracts, existing rules are verified with isolated positive and negative fixtures, and stable framework-neutral conventions are enforced without adding production business code. Keep logging out of the public contract policy while deferring logger-field enforcement until the generated service selects a logging API.

## Scope
- In scope:
  - Import `core` main output alongside `service` or `client` main output for cross-module architecture analysis.
  - Enforce root-independent, name-derived `*Service` to `*ServiceImpl` and `*ServiceClient` contract relationships, including indirect assignability.
  - Require direct `core` service contracts to be public interfaces; require direct server/client role types to be classes rather than interfaces.
  - Enforce the existing no-`Dto` contract-model convention.
  - Add isolated test-only fixtures that prove the positive and negative behavior of new and existing rules.
  - Document the logging policy and the rules intentionally deferred because they lack a stable framework-neutral boundary.
- Out of scope:
  - Production Java classes, example business types, runtime dependencies, application frameworks, or a selected logging API.
  - Requiring logger fields, logger names, or dependencies on SLF4J, Log4j, JUL, Spring, Jakarta, or another logging implementation.
  - Abstract/final class policy, implementation visibility, constructor-injection enforcement, field-finality enforcement, package-cycle slices, broad package allowlists, or guessed business-boundary rules.
  - ArchUnit duplication of the Gradle project dependency graph.

## Constraints
- The orchestrator will not push.
- The final commit will include every tracked and untracked worktree change present at finalization, including manual user changes.
- Preserve the production graph: `service`, `client`, `consumer`, and `producer` depend only on `core`; test wiring must not add a production dependency edge.
- Keep `src/main/java` free of Java classes; all proof fixtures belong under `src/test/java/architecture/fixture/`.
- Production architecture rules remain empty-safe with `allowEmptyShould(true)`.
- Rule logic and package matching must not contain `dev.nexcraft.foo`, `foo`, `bar`, or another assumed root/boundary name.
- Fixture imports must be explicit and isolated from production imports so fixtures cannot satisfy or violate the rules run against main output.
- Test classes, helpers, conditions, fixture documentation, and failure messages use English; test methods use camelCase.

## Orchestration Profile
- Profile: STRICT
- Risk level: ELEVATED
- Risk signals: shared Gradle test infrastructure, cross-module class-output import behavior, and custom ArchUnit conditions used by two implementation modules.
- Delegated phases:
  - Research: REQUIRED
  - Implementation: AGENT
  - Testing: AGENT
  - Review: AGENT
  - Walkthrough: AGENT
- Rationale: the runtime API remains unchanged, but the change couples `core`, `service`, and `client` test execution and introduces custom relationship logic whose false-positive and false-negative behavior must be demonstrated before finalization.

## Repository Evidence
- Research input: `docs/plan/architecture-hardening/research.md`
- Relevant modules and entry points:
  - `build.gradle.kts` — currently supplies every test task with only its owning main output through `architectureTest.mainClassesDirs`.
  - `service/src/test/java/architecture/ArchitectureTestSupport.java` and `client/src/test/java/architecture/ArchitectureTestSupport.java` — currently import one path set and therefore cannot distinguish implementation output from `core` contract output.
  - `core/src/test/java/architecture/CoreArchitectureTest.java` — currently enforces interface type and the `Service` suffix, but not public visibility or the no-`Dto` model rule.
  - `service/src/test/java/architecture/ServiceArchitectureTest.java` and `client/src/test/java/architecture/ClientArchitectureTest.java` — currently check role suffixes without proving contract existence or assignability.
  - Consumer and producer architecture tests — currently enforce their suffix rules only against empty production output.
- Existing contracts and public APIs:
  - `core` is the public contract module; direct `..service` role types are service interfaces and `..model..` contains contract models.
  - `service` and `client` already have production `implementation(project(":core"))` edges; those edges remain unchanged.
  - No production Java class exists, so the hardening must add no public API or example implementation.
- Existing test and build conventions:
  - `gradle.properties` pins ArchUnit 1.5.0 and JUnit 5.14.4.
  - All five modules use the standard Gradle `test` task; prior reports exist at `*/build/test-results/test/*.xml` and `*/build/reports/tests/test/index.html`.
  - `gradle/wrapper/gradle-wrapper.properties` pins Gradle 9.4.1.

## Standards Profile
- Selected standards:
  - `/Users/sean/.codex/skills/java-orchestrator/references/orchestrator/test-command-discovery.md`
  - `/Users/sean/.codex/skills/java-orchestrator/references/constitution/core/testing-and-quality.md`
  - `/Users/sean/.codex/skills/java-orchestrator/references/constitution/core/coding-standards.md`
- Service classification: none; the repository remains a framework-neutral provisioning template with no runtime application.
- Non-negotiable requirements carried into this plan:
  - Every rule behavior change has deterministic, local positive and negative coverage at the smallest test level.
  - Test fixtures use no external systems or mutable shared state and remain outside production source sets.
  - Rules operate on declared package responsibilities, not an organization root, placeholder name, logging framework, annotation framework, or inferred business boundary.

## Architecture Decisions
- Enforce now:
  - A top-level type directly in a `core` `..service` package is public, is an interface, and ends in `Service`.
  - A type in a `core` `..model..` package does not end in `Dto`; nested operation/view types such as `Create`, `Update`, and `Details` remain allowed.
  - Top-level types directly in `..service.impl` and `..service.client` are classes, retain their current suffix, resolve to a contract in `core`, and are directly or indirectly assignable to that contract.
  - Existing service dependency prohibitions and entity/client/consumer/producer suffix conventions remain enforced and gain fixture-based behavior tests.
- Contract matching algorithm:
  - For `P.service.impl.FooServiceImpl`, remove only the terminal package segment `.impl` and terminal class suffix `Impl`; expect `P.service.FooService`.
  - For `P.service.client.FooServiceClient`, remove only the terminal package segment `.client` and terminal class suffix `Client`; expect `P.service.FooService`.
  - Search only classes imported from the configured `core` output for the derived FQCN, require that type to be an interface, and compare the derived FQCN against `JavaClass#getAllClassesSelfIsAssignableTo()` by class name so an intermediate superclass may implement the contract.
  - Report distinct deterministic violations for a missing contract, a derived contract that is not an interface, and a role class that is not assignable to the contract. A malformed role suffix remains a violation of the existing suffix rule and must not make the relationship condition throw.
- Defer intentionally:
  - Logging: document that logging is not part of `core` contracts. Do not inspect fields or ban named logger packages until a logging API is selected; simple type/name matching would be incomplete and framework-specific.
  - Implementation visibility and abstract/final status: prohibit interfaces in implementation role packages, but permit package-private, abstract, open, or final classes to avoid constraining future framework/proxy choices.
  - Constructor injection and final fields: retain them as coding standards, not ArchUnit rules, until the template selects component semantics and an injection model.
  - Cycles and package leaks: do not create arbitrary slices or broad allowlists without a machine-readable application/business boundary; these would reject legitimate helpers or miss the intended boundary.
  - Gradle module direction: keep Gradle as its single source of truth rather than duplicating project dependency declarations in bytecode rules.

## Verification Plan

### Unit scope
- Status: REQUIRED
- Working directory: `/Users/sean/Documents/projects/java-service-template`
- Command: `./gradlew clean build`
- Gradle task: `test`
- Expected JUnit XML: `*/build/test-results/test/*.xml`
- Expected HTML report: `*/build/reports/tests/test/index.html`
- Triage command: `python3 /Users/sean/.codex/skills/test-failure-triage/scripts/render_test_failure_report.py --root /Users/sean/Documents/projects/java-service-template --scope unit --task test --prefer auto`
- Expected triage report: `docs/test-report/unit-index.md`
- Discovery evidence: all five subprojects apply `java-library`, the root configures standard `Test` tasks, and the current run has JUnit XML and HTML reports for each module's `test` task.

### Component or integration scope
- Status: N/A
- Working directory: N/A
- Command: N/A
- Gradle task: N/A
- Expected JUnit XML: N/A
- Expected HTML report: N/A
- Discovery evidence: the build defines no component/integration source set or Gradle task, and the research found no CI or repository command for that scope.

## Tasks

### T01 — Wire explicit core contract output into implementation-module tests
- Context: service/client relationship rules need both their own bytecode and `core` contract bytecode, while the other modules must remain module-local.
- Implementation notes:
  - Update `build.gradle.kts` so only the `service:test` and `client:test` tasks receive a path-separated `architectureTest.contractClassesDirs` property sourced from `project(":core")`'s main `SourceSet` output.
  - Configure the two test tasks to depend on the `core` classes lifecycle, declare the contract directories as task inputs, and prepare only missing build-output directories immediately before execution, matching the existing empty-main-output handling.
  - Update the service/client `ArchitectureTestSupport.java` files with separate imports for owning main classes, core contract classes, and their union. Parse both properties with the existing platform path-separator behavior and fail clearly when the contract property is absent.
  - Leave core/consumer/producer import helpers module-local. Do not use `importClasspath()`, scan dependency JARs, or add test/runtime dependencies between implementation modules.
- Test notes:
  - Verify service/client production rules receive the union while contract lookup is restricted to the explicit core directories.
  - Verify core, consumer, and producer tests continue to receive only `architectureTest.mainClassesDirs`.
- Acceptance criteria:
  - Service/client tests can identify a core contract by FQCN and inspect implementation assignability without changing any production configuration or dependency edge.
  - Empty core and implementation main outputs still execute all production architecture tests successfully.
- Done when: `:service:test` and `:client:test` run with both isolated path sets and the remaining module tests retain their current import boundary.
- Result:
  - Status: DONE
  - Result Notes: Root Gradle test wiring now gives only `service:test` and `client:test` the explicit `core` main output through `architectureTest.contractClassesDirs`, declares the output input, depends on `:core:classes`, and keeps core/consumer/producer module-local imports. Service/client helpers expose separate main, contract-only, combined, and fixture imports.

### T02 — Harden core contract rules
- Context: public cross-module contracts need a stable visibility/type convention, and the explicit no-`Dto` model convention is not currently machine-enforced.
- Implementation notes:
  - Refactor reusable rules into package-private `core/src/test/java/architecture/CoreArchitectureRules.java`; keep `CoreArchitectureTest.java` as the production-output conformance test.
  - For top-level classes directly in exact `..service` packages, require public visibility, interface type, and a simple name ending in `Service`, with empty-safe production evaluation.
  - For all types in `..model..`, reject simple names ending in `Dto`; do not reject nested model types or prescribe model shape.
- Test notes:
  - Add `CoreArchitectureRulesTest.java` and fixtures below `core/src/test/java/architecture/fixture/` covering a valid public service interface, non-interface service type, wrong suffix, non-public service interface, valid top-level/nested contract models, and an invalid `*Dto` model.
  - Import only each fixture case package/class set and assert both `hasViolation() == false` and `hasViolation() == true` paths against the same rule definitions used for production.
- Acceptance criteria:
  - Public interface, suffix, and no-`Dto` rules have at least one matching passing fixture and one matching failing fixture; nested operation/view model types remain valid.
  - No fixture appears under `core/src/main/java` or in the production import set.
- Done when: the core production test and rule-behavior test both pass and their JUnit report lists the new cases.
- Result:
  - Status: DONE
  - Result Notes: `CoreArchitectureRules` now enforces public top-level service interfaces with the `Service` suffix and rejects `Dto` model types while allowing nested operation/view names. `CoreArchitectureRulesTest` covers valid and invalid service/model fixtures; production Java remains empty.

### T03 — Enforce server implementation-to-contract relationships
- Context: a correctly suffixed server type can currently omit or implement the wrong core contract without detection.
- Implementation notes:
  - Refactor reusable service rules into package-private `service/src/test/java/architecture/ServiceArchitectureRules.java`; retain `ServiceArchitectureTest.java` for production-output conformance.
  - Add a package-private custom `ArchCondition<JavaClass>` under `service/src/test/java/architecture/` implementing the exact server matching algorithm from Architecture Decisions and using the core-only class set for contract existence/type checks.
  - Require direct top-level `..service.impl` role types to be classes, end in `ServiceImpl`, and satisfy the matching condition. Allow indirect implementation through a superclass; do not require public, non-abstract, or final classes.
  - Preserve the six existing endpoint/repository/mapper dependency prohibitions and the direct `..repository.entity` `Entity` suffix rule unchanged in scope and empty behavior.
- Test notes:
  - Add `ServiceArchitectureRulesTest.java` with neutral fixtures below `service/src/test/java/architecture/fixture/` using case-specific roots ending in `.service` and `.service.impl`.
  - Cover valid direct implementation, valid indirect implementation, missing contract, non-interface derived contract, non-assignable implementation, role interface, bad implementation suffix, and bad entity suffix.
  - Add isolated allowed/forbidden fixture graphs for every existing endpoint/repository/mapper edge; evaluate each prohibition independently so an intentional violation of one rule cannot mask another.
- Acceptance criteria:
  - `P.service.impl.XServiceImpl` passes only when `core` contains interface `P.service.XService` and the class is directly or indirectly assignable to it.
  - Every missing/wrong relationship produces a readable rule violation rather than an exception from suffix/package parsing.
  - Each retained dependency and naming rule has a matching pass and failure proof.
- Done when: service production conformance and all fixture-based rule behavior tests pass under `:service:test`.
- Result:
  - Status: DONE
  - Result Notes: `ServiceArchitectureRules` preserves all six dependency prohibitions and entity/suffix rules, adds class-role and name-derived core-contract checks, and `ServiceImplementationMatchesContract` reports missing, non-public/non-interface, non-assignable, and malformed cases deterministically. Fixture tests cover direct and inherited implementation, every relationship failure, role interfaces, entities, and each retained dependency edge.

### T04 — Enforce client implementation-to-contract relationships
- Context: remote implementations have the same unverified contract relationship as server implementations but use a distinct package and suffix transformation.
- Implementation notes:
  - Refactor reusable client rules into package-private `client/src/test/java/architecture/ClientArchitectureRules.java`; retain `ClientArchitectureTest.java` for production conformance.
  - Add a package-private custom condition under `client/src/test/java/architecture/` implementing the exact client matching algorithm from Architecture Decisions and using only the core contract class set for lookup.
  - Require direct top-level `..service.client` role types to be classes, end in `ServiceClient`, and be directly or indirectly assignable to the derived public core service interface. Do not impose implementation visibility, abstract/final, constructor, annotation, or field rules.
- Test notes:
  - Add `ClientArchitectureRulesTest.java` with isolated fixtures below `client/src/test/java/architecture/fixture/` for valid direct and indirect implementation, missing contract, non-interface contract, non-assignable implementation, role interface, and bad suffix.
  - Use distinct case roots so a contract from one fixture cannot accidentally satisfy another case; import implementation and contract fixture sets explicitly.
- Acceptance criteria:
  - `P.service.client.XServiceClient` passes only when the core-only fixture/output contains interface `P.service.XService` and assignability is direct or inherited.
  - Every positive and negative relationship branch is demonstrated without production source or framework assumptions.
- Done when: client production conformance and fixture rule behavior tests pass under `:client:test`.
- Result:
  - Status: DONE
  - Result Notes: `ClientArchitectureRules` and `ClientImplementationMatchesContract` enforce class roles, `ServiceClient` suffixes, and name-derived public core-contract assignability including inherited implementation. Fixture tests cover direct/indirect matches, missing/non-interface/non-assignable contracts, interface roles, and malformed suffixes.

### T05 — Add behavior proofs for messaging suffix rules
- Context: consumer and producer rules currently pass only because production is empty, so regressions in their predicates would be invisible.
- Implementation notes:
  - Extract package-private reusable rules into `ConsumerArchitectureRules.java` and `ProducerArchitectureRules.java`, retaining the existing production conformance test classes.
  - Add `ConsumerArchitectureRulesTest.java` and `ProducerArchitectureRulesTest.java` with isolated test-only top-level valid and invalid role fixtures under each module's `src/test/java/architecture/fixture/` tree.
  - Keep exact-package, top-level-only, root-independent, and empty-safe production semantics unchanged.
- Test notes:
  - Prove that `*Consumer`/`*Producer` in the direct role package passes, a wrong suffix in that package fails, and unrelated packages are not selected.
- Acceptance criteria:
  - Both messaging suffix rules have positive, negative, and out-of-scope fixture evidence while production source remains empty.
- Done when: consumer and producer production conformance and rule behavior tests pass and appear in JUnit reports.
- Result:
  - Status: DONE
  - Result Notes: Consumer and producer predicates were extracted into reusable package-private rule classes, with valid, invalid, and out-of-scope fixtures proving direct-package suffix behavior while production outputs remain empty-safe.

### T06 — Document enforced rules and intentional deferrals
- Context: maintainers need to know what the strengthened suite guarantees and why logging/cycle/style rules are not guessed by the template.
- Implementation notes:
  - Update `README.md` in English with the public core contract rule, no-`Dto` model rule, implementation class/suffix/contract-matching rules, inherited implementation allowance, and the retained service/messaging rules.
  - State that `core` contracts must remain logging-free at the API/design level, but the template deliberately has no logger field/type/package ArchUnit rule until a generated project chooses a logging API.
  - Record that implementation visibility/abstract/final policy, injection/finality checks, arbitrary package cycles, broad package allowlists/leaks, and cross-business-boundary rules are not enforced by this framework-neutral template.
  - Keep Gradle module dependency direction documented as Gradle-owned and retain the replaceable package-root instructions.
- Test notes:
  - Compare the README rule inventory with the actual rule definitions and ensure no deferred policy is described as machine-enforced.
- Acceptance criteria:
  - Documentation clearly separates enforced architecture from logging and other deferred conventions, without promising an unimplemented logger check.
- Done when: the README exactly reflects the final test suite and no root/framework assumption is introduced.
- Result:
  - Status: DONE
  - Result Notes: README now documents public/no-Dto core rules, server/client contract matching and inherited implementation, explicit core-output test wiring, the logging-free contract policy, and framework-neutral deferred policies without introducing a logger API or root/boundary heuristic.

## Execution Gates
- Implementation: choose **Start implementation** in the structured choice UI, or request plan revision/free-form changes.
- Review: choose the planned specialist review, return to implementation, or use free-form input.
- Walkthrough: choose the planned walkthrough agent, return to review, or use free-form input.
- Final commit: choose **Approve and commit** in the structured choice UI, or request revision/free-form changes.

## Global Acceptance
- Every task acceptance criterion is met.
- `./gradlew clean build` passes from the repository root with all five production architecture suites and their fixture behavior suites discovered.
- The dependency-free triage command returns usable clean unit evidence at `docs/test-report/unit-index.md`; exit code `2` is treated as missing evidence, not a pass.
- JUnit XML and HTML reports exist for every module at the mapped report paths.
- Direct and indirect server/client matches pass; missing, non-interface, non-assignable, wrong-role-type, and wrong-suffix fixtures fail their evaluated rule as intended.
- Existing dependency, entity, consumer, and producer rules have fixture-based positive and negative evidence.
- No Java class or runtime dependency is added to a production source/configuration, and no production module dependency changes.
- No logging API, root-package literal, business-name heuristic, broad allowlist, cycle rule, injection/finality rule, or framework-specific condition is introduced.
- Component/integration testing remains recorded as `N/A`, not as a pass.
- `docs/plan/architecture-hardening/review.md` has no open blocking findings from the required specialist reviewer.
- `docs/walkthrough/architecture-hardening.md` is complete and records the final verification evidence and approved Conventional Commit message.

## Risks and Impact
- Cross-project class-directory resolution can be order-sensitive; Gradle wiring must be lazy, declare the core output input, and depend on core class generation rather than reading an assumed build path.
- A combined importer could accidentally accept a same-named contract from the implementation module; relationship conditions must restrict contract existence/type lookup to the separately imported core directories.
- String-derived FQCN rules can fail unclearly on malformed names; suffix rules and custom conditions must return deterministic violations without substring exceptions.
- Fixture packages intentionally resemble architecture roles; production importers must remain path-bound to main output so test fixtures never influence actual conformance results.
- Abstract implementations and framework proxy requirements are intentionally left open; enforcing them now would reduce template portability.
- The logger policy is documented rather than machine-enforced, so generated repositories must add an API-specific rule when they select their logging stack.

## References
- `docs/plan/architecture-hardening/research.md`
- `README.md`
- `build.gradle.kts`
- `gradle.properties`
- `core/src/test/java/architecture/CoreArchitectureTest.java`
- `service/src/test/java/architecture/ServiceArchitectureTest.java`
- `client/src/test/java/architecture/ClientArchitectureTest.java`
- `consumer/src/test/java/architecture/ConsumerArchitectureTest.java`
- `producer/src/test/java/architecture/ProducerArchitectureTest.java`
- `/Users/sean/.codex/skills/java-orchestrator/references/planner/templates.md`
- `/Users/sean/.codex/skills/java-orchestrator/references/orchestrator/test-command-discovery.md`
- `/Users/sean/.codex/skills/java-orchestrator/references/constitution/core/testing-and-quality.md`
- `/Users/sean/.codex/skills/java-orchestrator/references/constitution/core/coding-standards.md`
