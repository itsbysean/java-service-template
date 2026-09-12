# Research — ArchUnit architecture tests

## Request

Add framework-neutral ArchUnit and JUnit 5 tests to the empty Gradle multi-module template while preserving empty-project success, avoiding hard-coded root or example package names, and enforcing only stable architecture rules.

## Research Mode

- Source: SINGLE_AGENT
- Additional model calls: 1, required by the STRICT profile for a build-infrastructure change

## Repository Snapshot

- Repository root: `/Users/sean/Documents/projects/java-service-template`
- Current branch: `feat/initial-project-structure`
- Worktree state: `README.md` is modified and the initial template files are untracked; `.idea/` and Gradle/build outputs are ignored and remain untouched.
- Relevant build roots and modules:
  - `build.gradle.kts` — shared Java 25 configuration
  - `core` — public contracts with no module dependencies
  - `service`, `client`, `consumer`, `producer` — implementation modules depending only on `core`

## Scope Map

| Area | Entry point or owner | Why it matters | Evidence |
|---|---|---|---|
| Module registration | `settings.gradle.kts` | Defines the five production modules | All requested modules are included |
| Shared test configuration | Root and module `build.gradle.kts` files | Must centralize versions without production dependencies | All modules use the built-in `java-library` plugin |
| Service architecture | `service/src/main/java/dev/nexcraft/foo` | Owns endpoint, implementation, repository, mapper, entity, config, and filter roles | Existing `.gitkeep` package tree |
| Other role naming | `client`, `consumer`, `producer` | Each module must inspect its own compiled output | Existing `.gitkeep` package trees |
| Documentation | `README.md` | Currently says ArchUnit is deferred | Architecture Conventions section |

## Execution and Data Flow

1. A module's standard Gradle `test` task starts the JUnit Platform test engine.
2. That module's architecture tests import its production classes and apply package dependency or scoped naming rules.
3. `./gradlew clean build` runs all five module build and test tasks from the root project.

## Public Contracts and Compatibility

| Contract | Current behavior | Constraint | Evidence |
|---|---|---|---|
| Production Java API | No Java classes exist | Do not add sample or marker production classes | Module source trees contain only `.gitkeep` files |
| Module dependency graph | Four implementation modules depend only on `core` | Preserve these edges and add test-only libraries | Module build files |
| Package root | `dev.nexcraft.foo` is a replaceable placeholder | Architecture rules must not embed this root as a string | README and source paths |

## Architecture Feasibility

- Negative service package rules are stable and can use generic package patterns: endpoint must not depend on service implementations, repositories, or mappers; repositories must not depend on endpoints or service implementations; mappers must not depend on endpoints.
- Separate positive “allowed dependency” rules are unnecessary. Restrictive allowlists would risk rejecting JDK types and future framework-neutral helpers.
- Exact role packages can enforce `*ServiceImpl`, `*ServiceClient`, `*Consumer`, `*Producer`, and `*Entity` without restricting `config` or unrelated helper packages.
- Every rule selecting a role package must use `allowEmptyShould(true)` so the initial empty template passes.
- Cross-boundary repository, mapper, and implementation restrictions cannot be implemented safely: the current package layout declares no generic boundary identifier. This must remain documented rather than approximated with `foo`/`bar` or a naming heuristic.

## Build and Test Evidence

### Unit scope

- Status: REQUIRED
- Working directory: `/Users/sean/Documents/projects/java-service-template`
- Command: `./gradlew clean build`
- Expected JUnit XML: `*/build/test-results/test/*.xml`
- Expected HTML report: `*/build/reports/tests/test/index.html`
- Discovery evidence: `./gradlew tasks --all` lists standard `test` tasks for all five Java modules.

### Component or integration scope

- Status: N/A
- Working directory: N/A
- Command: N/A
- Expected JUnit XML: N/A
- Expected HTML report: N/A
- Discovery evidence: no component or integration source sets, tasks, CI workflows, or repository commands exist.

## Candidate Impact Areas

| Area | Likely files or symbols | Evidence-backed reason |
|---|---|---|
| Test dependencies | Root and affected module build files | Add only ArchUnit/JUnit test dependencies and JUnit Platform configuration |
| Service rules | `service/src/test/java/.../architecture/` | Owns dependency and server-side naming rules |
| Module role naming | Module-specific architecture test packages | Client, consumer, and producer outputs require direct inspection |
| Documentation | `README.md` | Must describe implemented rules and the cross-boundary limitation |

## Uncertainties and User Decisions

- Resolved by evidence:
  - Empty role packages require empty-match allowance.
  - Gradle already enforces module direction structurally.
  - ArchUnit 1.5.0 is the latest official ArchUnit release and JUnit 5.14.4 is the current JUnit 5 release at research time.
- Requires user decision:
  - None; the request explicitly rejects unsafe cross-boundary heuristics.
- Remaining uncertainty:
  - The planner must choose a root-package-independent import mechanism that inspects only each module's production output.

## Evidence Index

- `settings.gradle.kts` — module registration
- `build.gradle.kts` and `gradle.properties` — Java 25 configuration
- Module build files — existing project dependency graph
- `*/src/main/java/dev/nexcraft/foo/...` — intended empty production package structure
- `README.md` — current architecture documentation
- `gradle/wrapper/gradle-wrapper.properties` — Gradle 9.4.1 wrapper
- `./gradlew tasks --all` — standard module test tasks
- `https://github.com/TNG/ArchUnit/releases` — ArchUnit release evidence
- `https://github.com/junit-team/junit-framework/releases` — JUnit 5 release evidence
