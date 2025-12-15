# HexaFun

A functional, declarative Java framework for building Hexagonal Architecture apps.

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

The [Fluent DSL](fluent.html) provides a concise, declarative way to compose use cases with type safety:

```java
// Define type-safe keys
public interface CounterUseCases {
    UseCaseKey<IncrementInput, Result<Counter>> INCREMENT =
        UseCaseKey.of("increment");
    UseCaseKey<AddInput, Result<Counter>> ADD =
        UseCaseKey.of("add");
}

// Build the app
HexaApp app = HexaFun.dsl()
    .useCase(INCREMENT)
        .validate(CounterValidators::validateIncrement)
        .handle(input -> Result.ok(input.counter().increment()))
    .useCase(ADD)
        .validate(CounterValidators::validateCounter)
        .validate(CounterValidators::validateAmount)    // Chained validators
        .handle(input -> Result.ok(input.counter().add(input.amount())))
    .build();

// Invoke with type safety
Result<Counter> result = app.invoke(INCREMENT, new IncrementInput(counter));
```

Key features:
- **Type-safe keys**: `UseCaseKey<I, O>` for compile-time checking
- **Clear naming**: `validate/handle` instead of `from/to`
- **Implicit closure**: No `.and()` chaining needed
- **Validator chaining**: Multiple `.validate()` calls
- **Port registry**: Type-safe dependency injection with `.withPort()`

See the [Fluent DSL Guide](fluent.html) for complete documentation, or follow
the [Tutorial](tutorial.html) to build a complete task manager from scratch.

---

## Core Concepts

| Concept              | How HexaFun Supports It                |
| -------------------- | -------------------------------------- |
| Use Cases            | `UseCase<I, O>` interface              |
| Type-Safe Keys       | `UseCaseKey<I, O>` for safe dispatch   |
| Input Validation     | `ValidationPort<I>` interface          |
| Validator Chaining   | Multiple `.validate()` calls           |
| Port Registry        | `port(Class<T>, impl)` for DI          |
| Functional Pipelines | Chain `.validate(...).handle(...)`     |
| Clean Architecture   | Strict separation of concerns          |
| Error Handling       | `Result<T>` monadic error handling     |
| Testing              | Declarative testing DSL                |

---

## Testing

Easily test your use cases with a fluent API:

```java
// Test successful execution
app.test(INCREMENT)
   .with(new IncrementInput(Counter.zero()))
   .expectOk(counter -> assertEquals(1, counter.value()));

// Test validation failure
app.test(ADD)
   .with(new AddInput(null, 10))
   .expectFailure(error -> assertEquals("Counter cannot be null", error));
```

---

## Packages

* `com.guinetik.hexafun.hexa` - Hexagonal ports (`UseCase`, `UseCaseKey`, `ValidationPort`)
* `com.guinetik.hexafun.fun` - Functional primitives (`Result`)
* `com.guinetik.hexafun` - Core application container (`HexaApp`, `HexaFun`)
* `com.guinetik.hexafun.testing` - Testing framework
* `com.guinetik.hexafun.examples` - Example applications
