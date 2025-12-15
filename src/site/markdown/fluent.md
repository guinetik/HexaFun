# The Fluent DSL

HexaFun provides a fluent DSL for composing use cases in a declarative, type-safe manner.
This page covers the design principles and usage patterns of the DSL.

---

## Design Principles

The DSL was designed around four key principles:

1. **Type Safety**: Compile-time verification of input/output types
2. **Clarity**: Method names that express intent (`validate`, `handle`)
3. **Minimal Ceremony**: No boilerplate connectors or explicit closures
4. **Composability**: Chain validators naturally

---

## Type-Safe Keys

Use cases are identified by `UseCaseKey<I, O>` which carries type information at compile time:

```java
// Define keys with their input and output types
public interface TaskUseCases {
    UseCaseKey<CreateInput, Result<Task>> CREATE = UseCaseKey.of("create");
    UseCaseKey<UpdateInput, Result<Task>> UPDATE = UseCaseKey.of("update");
    UseCaseKey<String, Result<Task>> DELETE = UseCaseKey.of("delete");
    UseCaseKey<Void, List<Task>> LIST = UseCaseKey.of("list");
}
```

This approach provides:

| Benefit | How |
|---------|-----|
| Compile-time safety | Wrong input type won't compile |
| Single source of truth | All signatures in one interface |
| Refactoring support | IDE can find all usages |
| Documentation | Types are self-documenting |

---

## Building Use Cases

### Basic Pattern: validate/handle

The core pattern separates validation from business logic:

```java
HexaApp app = HexaFun.dsl()
    .useCase(CREATE)
        .validate(this::validateInput)   // Returns Result<I>
        .handle(this::createTask)        // Runs only if validation passes
    .build();
```

The `validate` step returns `Result<I>`:
- On success: passes the validated input to `handle`
- On failure: short-circuits and returns the error

### Handler-Only Pattern

For use cases that don't need validation:

```java
HexaApp app = HexaFun.dsl()
    .useCase(LIST)
        .handle(input -> taskRepository.findAll())
    .build();
```

### Chained Validators

Multiple validators execute in order, short-circuiting on first failure:

```java
HexaApp app = HexaFun.dsl()
    .useCase(ADD)
        .validate(this::validateNotNull)     // First check
        .validate(this::validateAmountRange) // Only runs if first passes
        .validate(this::validatePermissions) // Only runs if both pass
        .handle(this::addAmount)
    .build();
```

This is equivalent to composing validators with `flatMap`, but more readable.

---

## Implicit Closure

The DSL uses implicit closure - each `useCase()` call automatically commits the previous one:

```java
// No .and() needed between use cases
HexaApp app = HexaFun.dsl()
    .useCase(CREATE)
        .validate(this::validateCreate)
        .handle(this::createTask)
    .useCase(UPDATE)                     // Previous use case auto-committed
        .validate(this::validateUpdate)
        .handle(this::updateTask)
    .useCase(DELETE)                     // Previous use case auto-committed
        .handle(this::deleteTask)
    .build();                            // Final use case committed here
```

This reduces visual noise and makes the DSL more natural to read.

---

## Port Registry

The DSL supports registering output ports (repositories, services, etc.) by type for dependency injection:

```java
HexaApp app = HexaFun.dsl()
    .withPort(TaskRepository.class, new InMemoryTaskRepository())
    .withPort(EmailService.class, new SmtpEmailService())
    .useCase(CREATE)
        .validate(this::validateInput)
        .handle(this::createTask)
    .build();
```

### Retrieving Ports

Retrieve ports by their type with compile-time safety:

```java
// Type-safe retrieval
TaskRepository repo = app.port(TaskRepository.class);

// Check if a port is registered
if (app.hasPort(EmailService.class)) {
    EmailService email = app.port(EmailService.class);
}

// List all registered port types
Set<Class<?>> portTypes = app.registeredPorts();
```

### Direct Registration

You can also register ports directly on a HexaApp:

```java
HexaApp app = HexaApp.create();
app.port(TaskRepository.class, new InMemoryTaskRepository())
   .port(EmailService.class, new SmtpEmailService());
```

### Benefits

| Benefit | Description |
|---------|-------------|
| Type safety | Compile-time checking prevents wrong types |
| Testability | Easy to swap implementations for tests |
| Decoupling | Use cases depend on interfaces, not implementations |
| Discoverability | `registeredPorts()` shows what's available |

---

## Invoking Use Cases

Invoke use cases using their type-safe keys:

```java
// Type-checked at compile time
Result<Task> result = app.invoke(CREATE, new CreateInput("My Task"));

// This won't compile - wrong input type:
// app.invoke(CREATE, "wrong type");  // Compile error!

// This won't compile - wrong return type:
// String result = app.invoke(CREATE, input);  // Compile error!
```

---

## Testing Use Cases

The testing DSL integrates with type-safe keys:

```java
// Test successful execution
app.test(CREATE)
   .with(new CreateInput("Test Task"))
   .expectOk(task -> {
       assertEquals("Test Task", task.getName());
       assertFalse(task.isCompleted());
   });

// Test validation failure
app.test(CREATE)
   .with(new CreateInput(""))  // Empty name
   .expectFailure(error -> {
       assertEquals("Name cannot be empty", error);
   });

// Test with transformation
app.test(CREATE)
   .with(new CreateInput("Test"))
   .map(Result::get)
   .map(Task::getName)
   .expectOk(name -> assertEquals("Test", name));
```

---

## Complete Example

Here's a complete example showing all DSL features:

```java
// 1. Define type-safe keys
public interface CounterUseCases {
    UseCaseKey<IncrementInput, Result<Counter>> INCREMENT =
        UseCaseKey.of("increment");
    UseCaseKey<AddInput, Result<Counter>> ADD =
        UseCaseKey.of("add");
}

// 2. Define validators
public class CounterValidators {
    public static Result<IncrementInput> validateIncrement(IncrementInput input) {
        if (input.counter() == null) {
            return Result.fail("Counter cannot be null");
        }
        return Result.ok(input);
    }

    public static Result<AddInput> validateCounter(AddInput input) {
        if (input.counter() == null) {
            return Result.fail("Counter cannot be null");
        }
        return Result.ok(input);
    }

    public static Result<AddInput> validateAmount(AddInput input) {
        if (input.amount() < -100 || input.amount() > 100) {
            return Result.fail("Amount must be between -100 and 100");
        }
        return Result.ok(input);
    }
}

// 3. Build the app
public class CounterApp {
    private final HexaApp app;

    public CounterApp() {
        this.app = HexaFun.dsl()
            .useCase(INCREMENT)
                .validate(CounterValidators::validateIncrement)
                .handle(input -> Result.ok(input.counter().increment()))
            .useCase(ADD)
                .validate(CounterValidators::validateCounter)
                .validate(CounterValidators::validateAmount)
                .handle(input -> Result.ok(input.counter().add(input.amount())))
            .build();
    }

    public Result<Counter> increment(Counter counter) {
        return app.invoke(INCREMENT, new IncrementInput(counter));
    }

    public Result<Counter> add(Counter counter, int amount) {
        return app.invoke(ADD, new AddInput(counter, amount));
    }
}

// 4. Test
@Test
void shouldIncrementCounter() {
    app.test(INCREMENT)
       .with(new IncrementInput(Counter.zero()))
       .expectOk(counter -> assertEquals(1, counter.value()));
}

@Test
void shouldFailOnNullCounter() {
    app.test(INCREMENT)
       .with(new IncrementInput(null))
       .expectFailure(error -> assertEquals("Counter cannot be null", error));
}

@Test
void shouldChainValidatorsForAdd() {
    app.test(ADD)
       .with(new AddInput(Counter.zero(), 500))  // Amount out of range
       .expectFailure(error -> assertEquals("Amount must be between -100 and 100", error));
}
```

---

## Migration from Old API

If you're migrating from an older version of HexaFun:

| Old API | New API |
|---------|---------|
| `.useCase("name")` | `.useCase(KEY)` where `KEY = UseCaseKey.of("name")` |
| `.from(validator)` | `.validate(validator)` |
| `.to(handler)` | `.handle(handler)` |
| `.and()` | *(not needed - implicit closure)* |
| `app.invoke("name", input)` | `app.invoke(KEY, input)` |
| `app.test("name")` | `app.test(KEY)` |

The new API is clearer, more type-safe, and requires less boilerplate.
