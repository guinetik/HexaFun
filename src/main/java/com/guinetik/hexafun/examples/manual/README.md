# Manual vs DSL Example

This example demonstrates two ways to create use cases with HexaFun:

1. **Manual Approach** (`ManualExample.java`): 
   - Create validation ports and use cases explicitly
   - Manual composition of validation and handling
   - Direct registration with the HexaApp

2. **DSL Approach** (`DslExample.java`):
   - Use the fluent builder pattern
   - Concise, declarative style
   - Automatic composition of validation and handling

Both approaches achieve the same result, but the DSL provides a more readable and maintainable way to define use cases.

## Domain Model

The example uses a simple `Counter` domain model with operations:
- `increment()` - increases the counter by 1
- `decrement()` - decreases the counter by 1
- `add(amount)` - changes the counter by the specified amount

## Running the Examples

To run the manual example:
```
mvn exec:java -Dexec.mainClass="com.guinetik.hexafun.examples.manual.ManualExample"
```

To run the DSL example:
```
mvn exec:java -Dexec.mainClass="com.guinetik.hexafun.examples.manual.DslExample"
```

## Key Differences

### Manual Approach
```java
// Create ports and use cases
ValidationPort<IncrementInput> incrementValidator = CounterOperations::validateCounter;
UseCase<IncrementInput, Result<Counter>> incrementHandler = 
        input -> Result.ok(input.getCounter().increment());

// Compose use cases manually
UseCase<IncrementInput, Result<Counter>> incrementUseCase = 
        input -> incrementValidator.validate(input).flatMap(i -> incrementHandler.apply(i));

// Register with the app
HexaApp app = HexaApp.create();
app.withUseCase("increment", incrementUseCase);
```

### DSL Approach
```java
// Create and compose in one step
HexaApp app = HexaFun.dsl()
    .<IncrementInput>useCase("increment")
        .from(CounterOperations::validateCounter)
        .to(input -> Result.ok(input.getCounter().increment()))
        .and()
    .build();
```

The DSL approach is more concise and readable but the manual approach offers finer control when needed.
