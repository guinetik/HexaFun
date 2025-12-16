# HexaFun ⚽
[![Maven Central](https://img.shields.io/maven-central/v/com.guinetik/hexafun-core.svg)](https://central.sonatype.com/artifact/com.guinetik/hexafun-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

> A functional, declarative Java framework for building Hexagonal Architecture apps with serious swagg.

Built for JDK 17+ with a clean API that puts your **use cases** front and center.
Forget the ceremony. Focus on composing behavior.

---

## What is HexaFun?

HexaFun brings **Hexagonal Architecture** into the functional age with minimal boilerplate and maximum clarity.

* Define use cases with clear port interfaces
* Pure business logic, no frameworks leaking in
* Plug in real adapters (HTTP, DB, Messaging) when *you* want
* Composable, testable, functional-by-design

---

## Installation

Add HexaFun to your Maven project:

```xml
<dependency>
    <groupId>com.guinetik</groupId>
    <artifactId>hexafun-core</artifactId>
    <version>1.0.1</version>
</dependency>
```

Or with Gradle:

```groovy
implementation 'com.guinetik:hexafun-core:1.0.1'
```

**Requirements:** Java 17+

---

## Example Applications

| Example | Description |
|---------|-------------|
| **Counter** | Simple increment/add with validation |
| **Tasks** | Kanban board TUI (TODO → DOING → DONE) |
| **Sysmon** | System monitor with 4 output adapters (TUI, CLI, JSON, Prometheus) |

Run the interactive launcher:
```shell
mvn exec:java -pl hexafun-examples
```

Or launch a specific example:
```shell
mvn exec:java -pl hexafun-examples -Dexec.args="sysmon"
```

---

## Architecture Overview

```
                     ┌────────────────────────┐
                     │   Driving Adapter      │  (REST / CLI, Events)
                     └────────▲───────────────┘
                              │
                        Input Port (UseCase<I, O>)
                              │
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│             ┌──────────────┐      ┌───────────────────┐     │
│             │  Use Cases   │ ◄──► │   Domain Model    │     │
│             └──────────────┘      └───────────────────┘     │
│                                                             │
└────────────────────▼──────────────▼─────────────────────────┘
                     │              │
              Output Port     Output Port
               (e.g. DB)        (e.g. Email)
                     │              │
                 ┌───▼────┐     ┌───▼──────┐
                 │ Adapter│     │ Adapter  │
                 └────────┘     └──────────┘

```

Hexagonal Architecture (also known as Ports and Adapters) is a software design pattern that emphasizes organizing application logic around a core domain, isolated from external concerns. HexaFun is a Java framework that implements this architectural pattern with a functional programming approach.

### Hexagonal Architecture Core Concepts

1. **Domain-Centric Design**: Business logic is at the center, protected from external dependencies
2. **Ports**: Interfaces that define how the application interacts with the outside world
   - **Input/Primary Ports**: How external actors communicate with the application
   - **Output/Secondary Ports**: How the application communicates with external systems
3. **Adapters**: Implementations of ports that connect the application to specific technologies
   - **Primary Adapters**: Drive the application (REST controllers, CLI interfaces)
   - **Secondary Adapters**: Provide infrastructure services (databases, external APIs)

### How HexaFun Implements Hexagonal Architecture

HexaFun modernizes Hexagonal Architecture with functional programming principles:

1. **Functional Core Primitives**:
   - `UseCase<I, O>`: Core business logic operations with clean input/output contracts
   - `UseCaseKey<I, O>`: Type-safe keys for compile-time dispatch safety
   - `ValidationPort<I>`: Input validation with clear error boundaries
   - Port Registry: Type-safe dependency injection for external dependencies

2. **Functional Error Handling**:
   - Uses the `Result<T>` monad to handle errors without exceptions
   - Enables functional composition through `map` and `flatMap` methods

3. **Simplified API**:
   - Minimal interfaces designed for Java's functional style
   - All interfaces are `@FunctionalInterface` for lambda support
   - Method references enable concise code: `this::validateInput`

4. **Fluent DSL** for use case composition:
   ```java
   // Define type-safe keys
   UseCaseKey<Input, Result<Output>> CREATE = UseCaseKey.of("create");

   HexaApp app = HexaFun.dsl()
       .useCase(CREATE)
           .validate(this::validateInput)
           .handle(this::executeUseCase)
       .build();
   ```

5. **Application Container**:
   - `HexaApp` manages and orchestrates use cases and adapters
   - Registry pattern for looking up use cases by name
   - Clear separation between definition and execution

6. **Testing Support**:
   - Declarative testing DSL for use cases
   - Mock adapters for clean unit testing
   - In-memory repositories for integration testing

HexaFun removes much of the boilerplate traditionally associated with Hexagonal Architecture while preserving its core benefits: separation of concerns, testability, and flexibility to change external implementations.

---

## Getting Started

### Core Primitives

HexaFun is built around a few simple interfaces:

#### 1. Define your ports

```java
// Core business logic (use case)
public interface UseCase<I, O> {
    O apply(I input);
}

// Input validation
public interface ValidationPort<I> {
    Result<I> validate(I input);
}

// External dependencies - define your own interfaces
public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
}
```

#### 2. Define type-safe keys

```java
// Type-safe keys carry input/output types at compile time
UseCaseKey<CreateInput, Result<Entity>> CREATE = UseCaseKey.of("create");
UseCaseKey<ListInput, List<Entity>> LIST = UseCaseKey.of("list");
```

#### 3. Build your app with the Fluent DSL

```java
HexaApp app = HexaFun.dsl()
    .useCase(CREATE)
        .validate(this::validateInput)      // validation port
        .handle(this::createEntity)         // use case implementation
    .useCase(LIST)
        .handle(this::listAllItems)         // no validation needed
    .build();
```

#### 4. Invoke use cases with type safety

```java
// Compile-time type checking - wrong input type won't compile
Result<Entity> result = app.invoke(CREATE, new CreateInput("test"));
```

---

## Supported Concepts

| Concept              | How HexaFun Supports It                |
| -------------------- | -------------------------------------- |
| Use Cases            | `UseCase<I, O>` interface              |
| Type-Safe Keys       | `UseCaseKey<I, O>` for safe dispatch   |
| Input Validation     | `ValidationPort<I>` interface          |
| Validator Chaining   | Multiple `.validate()` calls           |
| External Systems     | Port registry with `port(Class, impl)` |
| Functional Pipelines | Chain `.validate(...).handle(...)`     |
| Clean Architecture   | Strict separation of concerns          |
| Error Handling       | `Result<T>` monadic error handling     |
| Testing              | Declarative testing DSL                |

---

## Test Mode

Easily test your use cases with a fluent API:

```java
app.test(CREATE)
   .with(new CreateInput("test"))
   .expectOk(entity -> assertNotNull(entity.getId()));

app.test(VALIDATE)
   .with(invalidInput)
   .expectFailure(error -> assertEquals("Invalid input", error));
```

Check out the `CounterAppTest` in the examples for a working demonstration of the testing API.

---

## Roadmap

* [ ] Adapter integrations (REST, CLI, Events)
* [ ] Repository interface and built-in in-memory repo helpers
* [x] Test DSL
* [x] Type-safe use case keys
* [x] Validator chaining
* [x] Output port registry (`port(TaskRepo.class, impl)`)
* [x] Query registered use cases (`registeredUseCases()`)
* [x] Query registered ports (`registeredPorts()`)
* [ ] Modular grouping support

---

## Why Use HexaFun?

* Functional by default
* Framework-agnostic
* Clean and declarative
* Test-friendly
* Type-safe dispatch
* Pure Hexagonal vibes

---

## Packages

* `com.guinetik.hexafun.hexa` – Hexagonal ports (`UseCase`, `UseCaseKey`, `ValidationPort`)
* `com.guinetik.hexafun.fun` – Functional primitives (`Result`)
* `com.guinetik.hexafun` – Core application container (`HexaApp`, `HexaFun`)
* `com.guinetik.hexafun.testing` – Testing framework
* `com.guinetik.hexafun.examples` – Example applications
