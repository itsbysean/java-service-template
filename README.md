# Java Service Template

A minimal, framework-neutral Gradle template for Java backend services and microservices.

## Purpose

This repository provides a consistent multi-module starting point for new Java services. It defines contract and implementation boundaries without selecting an application framework, persistence technology, or messaging platform.

Application and business code intentionally start empty. The template contains no example controllers, services, repositories, entities, domain models, or application entry points.

## Modules

| Module | Responsibility |
| --- | --- |
| `core` | Public service contracts, contract models, and contract exceptions. |
| `service` | Server-side implementations, inbound endpoints, persistence, mapping, configuration, and filters. |
| `client` | Remote implementations of contracts declared by `core`. |
| `consumer` | Optional inbound asynchronous message handling. |
| `producer` | Optional outbound asynchronous message publishing. |

The initial dependency graph is deliberately small:

```text
service  -> core
client   -> core
consumer -> core
producer -> core
```

`core` must not depend on an implementation module, and implementation modules do not depend on each other.

## Package Structure

Every module uses `dev.nexcraft.foo` as its placeholder base package. After provisioning a repository, replace the final `foo` package segment with the service name.

Each path below is relative to its module's `src/main/java/dev/nexcraft/foo` directory:

```text
core
├── service
├── model
└── exception

service
├── service/impl
├── endpoint
├── repository
│   └── entity
├── mapper
├── config
└── filter

client
├── service/client
└── config

consumer
├── consumer
└── config

producer
├── producer
└── config
```

Empty package directories are preserved with `.gitkeep` files until application code is added.

## Naming Conventions

| Responsibility | Convention |
| --- | --- |
| Service contract | `FooService` |
| Server-side implementation | `FooServiceImpl` |
| Client-side implementation | `FooServiceClient` |
| Message consumer | `FooConsumer` |
| Message producer | `FooProducer` |
| Persistence entity | `FooEntity` |

Service contract models do not use the `Dto` suffix. A contract model may use nested types for operation-specific representations, such as `Foo.Create`, `Foo.Update`, and `Foo.Details`.

## Build

The project requires Java 25 and uses the Gradle Wrapper:

```shell
./gradlew clean build
```

Production source sets have no external dependencies. Test configurations use ArchUnit and JUnit 5 only for the architecture checks.

## Architecture Conventions

Architecture tests use ArchUnit and JUnit 5. They import each module's compiled production output; service and client tests additionally import the explicitly configured `core` output so contract relationships can be checked. Empty production modules remain valid and future package roots can be replaced without changing the rules.

The tests enforce these conventions:

- A top-level type directly in a `service` package in `core` must be a public interface whose name ends with `Service`.
- A type in a `core` `model` package must not use the `Dto` suffix; nested operation/view types remain allowed.
- A top-level type directly in a `service.impl` package must be a class named with the `ServiceImpl` suffix and must implement the name-derived public `core` service contract.
- A top-level type directly in a `service.client` package must be a class named with the `ServiceClient` suffix and must implement the name-derived public `core` service contract.
- Server and client contract matching allows indirect implementation through an intermediate superclass.
- A top-level type directly in a `repository.entity` package must end with `Entity`.
- A top-level type directly in a `consumer` package must end with `Consumer`.
- A top-level type directly in a `producer` package must end with `Producer`.
- Classes in `endpoint` packages must not depend on `service.impl`, `repository`, or `mapper` packages.
- Classes in `repository` packages must not depend on `endpoint` or `service.impl` packages.
- Classes in `mapper` packages must not depend on `endpoint` packages.

The dependency rules are intentionally prohibitions. Endpoint-to-service-contract and service-implementation-to-repository/mapper dependencies remain allowed. Gradle enforces the module-level direction: `service`, `client`, `consumer`, and `producer` depend on `core`, while `core` has no implementation-module dependency.

Core contracts are expected to remain logging-free at the API/design level. This template deliberately does not require logger fields or forbid a logger type because no logging API is selected; a generated service should add an API-specific rule after choosing its logging stack.

The template also intentionally defers implementation visibility and abstract/final policies, constructor-injection and field-finality checks, arbitrary package-cycle rules, broad package allowlists or leak rules, and cross-business-boundary checks. Those policies require framework, application, or boundary decisions that this framework-neutral provisioning template does not make.

Direct access across separate business boundaries to another boundary's repository, mapper, or implementation is an architectural convention, but it is not machine-enforced yet. The current package layout has no generic boundary identifier, so the tests do not infer one from the replaceable package root or from names such as `foo` or `bar`.
