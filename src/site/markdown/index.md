# HexaFun

A functional, declarative Java framework for building Hexagonal Architecture apps.

Built for JDK 17+ with a clean API that puts your **use cases** front and center.
Forget the ceremony. Focus on composing behavior.

---

## What is HexaFun?

HexaFun brings **Hexagonal Architecture** into the functional age — with minimal boilerplate and maximum clarity.

* Define use cases with clear port interfaces
* Pure business logic, no frameworks leaking in
* Plug in real adapters (HTTP, DB, Messaging) when *you* want
* Composable, testable, functional-by-design

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

Hexagonal Architecture (also known as Ports and Adapters) organizes application logic around a core domain, isolated from external concerns.

---

## Fluent DSL Example

The DSL provides a concise, declarative way to compose use cases:

```java
HexaApp app = HexaFun.dsl()
    // Create a use case with the name "increment"
    .<IncrementInput>useCase("increment")
        .from(CounterOperations::validateCounter)
        .to(input -> Result.ok(input.getCounter().increment()))
        .and()
    // Create a use case with the name "decrement"
    .<DecrementInput>useCase("decrement")
        .from(CounterOperations::validateCounter)
        .to(input -> Result.ok(input.getCounter().decrement()))
        .and()
    // Create a use case with the name "add"
    .<AddInput>useCase("add")
        .from(input -> {
            Result<AddInput> counterResult = CounterOperations.validateCounter(input);
            if (counterResult.isFailure()) {
                return counterResult;
            }
            return CounterOperations.validateAmount(input);
        })
        .to(input -> Result.ok(input.getCounter().add(input.getAmount())))
        .and()
    .build();

// Invoke use cases by name
Counter initial = Counter.of(10);
Result<Counter> result = app.invoke("increment", new IncrementInput(initial));
```

---

## Core Concepts

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

## Testing

Easily test your use cases with a fluent API:

```java
app.test("createTask")
   .with(new CreateTaskInput("Study HexaFun", "Important"))
   .expectOk(task -> assertFalse(task.isCompleted()));
```

---

## Packages

* `com.guinetik.hexafun.hexa` – Hexagonal ports (`UseCase`, `InputPort`, etc)
* `com.guinetik.hexafun.fun` – Functional primitives (`Result`, etc)
* `com.guinetik.hexafun` – Core application container (`HexaApp`)
* `com.guinetik.hexafun.testing` – Testing framework
* `com.guinetik.hexafun.examples` – Example applications
