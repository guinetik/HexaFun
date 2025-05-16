# HexaFun 🧹

> A functional, declarative Java framework for building Hexagonal Architecture apps with serious swagg.

Built for JDK 17+ with a clean API that puts your **use cases** front and center.
Forget the ceremony. Focus on composing behavior.

---

## 🔷 What is HexaFun?

HexaFun brings **Hexagonal Architecture** into the functional age — with minimal boilerplate and maximum clarity.

* 🚪 Define use cases with clear port interfaces
* 🧠 Pure business logic, no frameworks leaking in
* 🔌 Plug in real adapters (HTTP, DB, Messaging) when *you* want
* 💡 Composable, testable, functional-by-design

---

## 🧪 Example Applications

- **TodoApp**: A complete Todo application in `com.guinetik.hexafun.examples.todo`
- **Manual vs DSL**: Compare both approaches in `com.guinetik.hexafun.examples.manual`

Run the TodoAppDemo class to see an interactive console UI in action!
```shell
mvn exec:java
```

---

## 🧱 Architecture Overview

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
   - `InputPort<I, O>`: Primary/driving ports for application entry points
   - `OutputPort<I, O>`: Secondary/driven ports for external dependencies
   - `ValidationPort<I>`: Input validation with clear error boundaries

2. **Functional Error Handling**:
   - Uses the `Result<T>` monad to handle errors without exceptions
   - Enables functional composition through `map` and `flatMap` methods

3. **Simplified API**:
   - Minimal interfaces designed for Java's functional style
   - All interfaces are `@FunctionalInterface` for lambda support
   - Method references enable concise code: `this::validateInput`

4. **Fluent DSL** for use case composition:
   ```java
   HexaApp app = HexaFun.dsl()
       .useCase("createTask")
           .from(this::validateInput)
           .to(this::executeUseCase)
           .and()
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

## 🚀 Getting Started

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

// External dependencies (repositories, services)
public interface OutputPort<I, O> {
    O apply(I input);
}
```

#### 2. Manual registration

```java
// Create use cases
ValidationPort<CreateInput> validator = this::validateInput;
UseCase<CreateInput, Result<Entity>> handler = this::createEntity;

// Compose use cases
UseCase<CreateInput, Result<Entity>> useCase = 
    input -> validator.validate(input).flatMap(i -> handler.apply(i));

// Register in the app
HexaApp app = HexaApp.create();
app.withUseCase("createEntity", useCase);
```

#### 3. Use the app

```java
Result<Entity> result = app.invoke("createEntity", input);
```

### Fluent DSL

For a more concise and declarative approach, use the DSL:

```java
HexaApp app = HexaFun.dsl()
    .useCase("createTask")
        .from(this::validateInput)          // validation port
        .to(this::executeUseCase)           // use case implementation
        .and()
    .useCase("listTasks")
        .to(this::listAllItems)             // no validation needed
        .and()
    .build();
```

---

## ✅ Supported Concepts

| Concept              | How HexaFun Supports It            |
| -------------------- | ---------------------------------- |
| Use Cases            | `UseCase<I, O>` interface          |
| Input Validation     | `ValidationPort<I>` interface      |
| External Systems     | `OutputPort<I, O>` interface       |
| Functional Pipelines | Chain `.from(...).to(...)`         |
| Clean Architecture   | Strict separation of concerns      |
| Error Handling       | `Result<T>` monadic error handling |
| Testing              | Declarative testing DSL            |

---

## 🤪 Test Mode

Easily test your use cases with a fluent API:

```java
app.test("createTask")
   .with(new CreateTaskInput("Study HexaFun", "Important"))
   .expectOk(task -> assertFalse(task.isCompleted()));
```

Check out the `TodoAppTest` in the examples for a working demonstration of the testing API.

---

## 🔮 Roadmap

* [ ] Output port registry (e.g., `port(TaskRepo.class, impl)`)
* [ ] Adapter integrations (REST, CLI, Events)
* [ ] Repository interface and built-in in-memory repo helpers
* [x] Test DSL
* [ ] Modular grouping support

---

## ✨ Why Use HexaFun?

✅ Functional by default  
✅ Framework-agnostic  
✅ Clean and declarative  
✅ Test-friendly  
✅ Pure Hexagonal vibes  

---

## 📦 Packages

* `com.guinetik.hexafun.hexa` – Hexagonal ports (`UseCase`, `InputPort`, etc)
* `com.guinetik.hexafun.fun` – Functional primitives (`Result`, etc)
* `com.guinetik.hexafun` – Core application container (`HexaApp`)
* `com.guinetik.hexafun.testing` – Testing framework
* `com.guinetik.hexafun.examples` – Example applications
