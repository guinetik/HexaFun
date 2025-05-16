# Manual vs DSL Approach

This example demonstrates two ways to build applications with HexaFun:

1. **Primitive Approach** (`ManualExample.java`): 
   - Work directly with the core Hexagonal Architecture components
   - Create and compose ports and adapters explicitly
   - Full control over the wiring of components

2. **DSL Approach** (`DslExample.java`):
   - Use the fluent builder pattern
   - Concise, declarative style
   - Automatic composition of components

Both approaches achieve the same result, but they serve different purposes and preferences.

## Domain Model

The example uses a simple `Counter` domain model with operations:
- `increment()` - increases the counter by 1
- `decrement()` - decreases the counter by 1
- `add(amount)` - changes the counter by the specified amount

## Running the Examples

To run the primitive approach example:
```
mvn exec:java -Dexec.mainClass="com.guinetik.hexafun.examples.manual.ManualExample"
```

To run the DSL example:
```
mvn exec:java -Dexec.mainClass="com.guinetik.hexafun.examples.manual.DslExample"
```

## Core Primitives

At the heart of HexaFun are the core hexagonal architecture primitives:

### Ports
```java
// Primary/driving port
public interface InputPort<I, O> {
    O handle(I input);
}

// Secondary/driven port
public interface OutputPort<I, O> {
    O apply(I input);
}

// Core business logic interface
public interface UseCase<I, O> {
    O apply(I input);
}

// Validation interface
public interface ValidationPort<I> {
    Result<I> validate(I input);
}
```

### Manual Composition
```java
// 1. Create validation port
ValidationPort<IncrementInput> incrementValidator = CounterOperations::validateCounter;

// 2. Create use case handler
UseCase<IncrementInput, Result<Counter>> incrementHandler = 
        input -> Result.ok(input.getCounter().increment());

// 3. Compose use cases manually
UseCase<IncrementInput, Result<Counter>> incrementUseCase = 
        input -> incrementValidator.validate(input).flatMap(i -> incrementHandler.apply(i));

// 4. Register with the app
HexaApp app = HexaApp.create();
app.withUseCase("increment", incrementUseCase);
```

## DSL for Simplified Composition

The DSL provides a more expressive way to achieve the same result:

```java
// All-in-one definition
HexaApp app = HexaFun.dsl()
    .<IncrementInput>useCase("increment")
        .from(CounterOperations::validateCounter)             // validation
        .to(input -> Result.ok(input.getCounter().increment())) // handling
        .and()
    .build();
```

## When to Use Each Approach

- **Use the primitive approach when**:
  - You need fine-grained control over component composition
  - You're extending the framework with custom behaviors
  - You're integrating with other frameworks or systems
  - You're writing tests that mock specific components

- **Use the DSL approach when**:
  - You want cleaner, more readable code
  - Your use cases follow standard patterns
  - You're defining multiple related use cases
  - You want to focus on the business logic rather than the wiring
