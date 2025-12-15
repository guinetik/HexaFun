# Tutorial: Building a Task Manager with HexaFun

This tutorial walks you through building a complete task management application
using HexaFun's functional hexagonal architecture approach. By the end, you'll have
a fully functional Kanban-style task manager with a beautiful terminal UI.

![Task Manager TUI](images/tui.png)

---

## What We're Building

A task manager that supports:

- **Kanban workflow**: TODO → DOING → DONE
- **CRUD operations**: Create, Read, Update, Delete tasks
- **Validation**: Title required, length limits
- **Multiple adapters**: Terminal UI today, REST API tomorrow

The architecture separates **what** your app does (use cases) from **how** it's accessed
(adapters), making it trivial to add new interfaces without touching business logic.

---

## Part 1: The Domain Model

Start with an immutable domain model. In HexaFun, your domain is pure Java with no
framework dependencies:

```java
public record Task(
    String id,
    String title,
    String description,
    TaskStatus status
) {
    public static Task create(String title, String description) {
        return new Task(
            UUID.randomUUID().toString(),
            title,
            description,
            TaskStatus.TODO
        );
    }

    public boolean completed() {
        return status == TaskStatus.DONE;
    }

    public Task start() {
        return new Task(id, title, description, TaskStatus.DOING);
    }

    public Task complete() {
        return new Task(id, title, description, TaskStatus.DONE);
    }

    public Task withTitle(String newTitle) {
        return new Task(id, newTitle, description, status);
    }
}
```

```java
public enum TaskStatus {
    TODO, DOING, DONE
}
```

Key design choices:
- **Immutable records** - State changes return new instances
- **Workflow methods** - `start()`, `complete()` encode valid transitions
- **No framework dependencies** - Pure domain logic

---

## Part 2: Define Your Use Cases

HexaFun uses type-safe keys to identify use cases. Define them in an interface:

```java
public interface TaskUseCases {

    UseCaseKey<CreateTask, Result<Task>> CREATE =
        UseCaseKey.of("createTask");

    UseCaseKey<StartTask, Result<Task>> START =
        UseCaseKey.of("startTask");

    UseCaseKey<CompleteTask, Result<Task>> COMPLETE =
        UseCaseKey.of("completeTask");

    UseCaseKey<DeleteTask, Result<Boolean>> DELETE =
        UseCaseKey.of("deleteTask");

    UseCaseKey<Void, List<Task>> LIST =
        UseCaseKey.of("listTasks");
}
```

Each key captures:
- **Input type** (e.g., `CreateTask`)
- **Output type** (e.g., `Result<Task>`)
- **Name** for debugging/logging

This gives you compile-time safety when invoking use cases later.

---

## Part 3: Input Records

Define simple records for each use case's input:

```java
public interface TaskInputs {

    record CreateTask(String title, String description) {}

    record StartTask(String taskId) {}

    record CompleteTask(String taskId) {}

    record UpdateTask(String taskId, String title, String description) {}

    record DeleteTask(String taskId) {}
}
```

Records are perfect for use case inputs:
- Immutable by default
- Built-in `equals`, `hashCode`, `toString`
- Clear, self-documenting structure

---

## Part 4: The Output Port (Repository)

Define what your application needs from the outside world:

```java
public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
    List<Task> findAll();
    boolean delete(String id);
}
```

This is an **output port** - an interface your domain defines that adapters implement.
The domain doesn't know if tasks are stored in memory, PostgreSQL, or MongoDB.

Here's a simple in-memory implementation for development:

```java
public class InMemoryTaskRepository implements TaskRepository {
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        tasks.put(task.id(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public boolean delete(String id) {
        return tasks.remove(id) != null;
    }
}
```

---

## Part 5: Validation

Validators are pure functions that return `Result<Input>`:

```java
public class TaskValidators {

    public static Result<CreateTask> validateCreateTitle(CreateTask input) {
        if (input.title() == null || input.title().isBlank()) {
            return Result.fail("Title cannot be empty");
        }
        return Result.ok(input);
    }

    public static Result<CreateTask> validateCreateTitleLength(CreateTask input) {
        if (input.title().length() > 100) {
            return Result.fail("Title must be 100 characters or less");
        }
        return Result.ok(input);
    }

    public static Result<StartTask> validateStartTaskId(StartTask input) {
        if (input.taskId() == null || input.taskId().isBlank()) {
            return Result.fail("Task ID cannot be empty");
        }
        return Result.ok(input);
    }

    // ... more validators
}
```

Validators are:
- **Pure functions** - No side effects, just input → Result
- **Chainable** - Multiple validators run in sequence
- **Short-circuiting** - First failure stops the chain

---

## Part 6: Composing the Application

Now wire everything together with HexaFun's fluent DSL:

```java
public class TaskApp {

    private final HexaApp app;

    public TaskApp(TaskRepository repository) {
        this.app = HexaFun.dsl()
            // Register the repository as an output port
            .withPort(TaskRepository.class, repository)

            // CREATE: validate title, then save
            .useCase(CREATE)
                .validate(TaskValidators::validateCreateTitle)
                .validate(TaskValidators::validateCreateTitleLength)
                .handle(input -> {
                    Task task = Task.create(input.title(), input.description());
                    return Result.ok(repository.save(task));
                })

            // START: validate ID, find task, move to DOING
            .useCase(START)
                .validate(TaskValidators::validateStartTaskId)
                .handle(input ->
                    repository.findById(input.taskId())
                        .map(task -> Result.ok(repository.save(task.start())))
                        .orElse(Result.fail("Task not found: " + input.taskId()))
                )

            // COMPLETE: validate ID, find task, move to DONE
            .useCase(COMPLETE)
                .validate(TaskValidators::validateCompleteTaskId)
                .handle(input ->
                    repository.findById(input.taskId())
                        .map(task -> Result.ok(repository.save(task.complete())))
                        .orElse(Result.fail("Task not found: " + input.taskId()))
                )

            // DELETE: validate ID, remove from repo
            .useCase(DELETE)
                .validate(TaskValidators::validateDeleteTaskId)
                .handle(input -> Result.ok(repository.delete(input.taskId())))

            // LIST: no validation, just return all tasks
            .useCase(LIST)
                .handle(input -> repository.findAll())

            .build();
    }

    // Factory method for in-memory version
    public static TaskApp withInMemoryRepo() {
        return new TaskApp(new InMemoryTaskRepository());
    }
}
```

The DSL features:
- **Port registry** - `withPort()` for dependency injection
- **Implicit closure** - No `.and()` between use cases
- **Validator chaining** - Multiple `.validate()` calls
- **Clear separation** - Validation before handling

---

## Part 7: The Public API

Expose a clean API that hides the HexaFun internals:

```java
public class TaskApp {
    // ... constructor from above ...

    public Result<Task> createTask(String title, String description) {
        return app.invoke(CREATE, new CreateTask(title, description));
    }

    public Result<Task> startTask(String taskId) {
        return app.invoke(START, new StartTask(taskId));
    }

    public Result<Task> completeTask(String taskId) {
        return app.invoke(COMPLETE, new CompleteTask(taskId));
    }

    public Result<Boolean> deleteTask(String taskId) {
        return app.invoke(DELETE, new DeleteTask(taskId));
    }

    public List<Task> listTasks() {
        return app.invoke(LIST, null);
    }

    // Access to ports for advanced use cases
    public TaskRepository getRepository() {
        return app.port(TaskRepository.class);
    }
}
```

Consumers of `TaskApp` don't need to know about HexaFun at all. They just call methods
and get `Result<T>` back.

---

## Part 8: Adding a Terminal UI (Driving Adapter)

Here's where hexagonal architecture shines. The `TaskApp` knows nothing about how
it's being used. Let's add a terminal UI as a **driving adapter**:

```java
public class TasksTUI {

    private final TaskApp app;

    public TasksTUI(TaskApp app) {
        this.app = app;
    }

    public void run() {
        State state = State.initial(app);

        while (state.running()) {
            render(state);
            String input = readLine();
            state = processInput(state, input);
        }
    }

    private State processInput(State state, String input) {
        Result<State> result = switch (input.toLowerCase()) {
            case "a", "add" -> handleAdd(state);
            case "s", "start" -> handleStart(state);
            case "c", "complete" -> handleComplete(state);
            case "d", "delete" -> handleDelete(state);
            case "q", "quit" -> Result.ok(state.stop());
            default -> Result.ok(state.withStatus("Unknown command", RED));
        };

        return result.fold(
            error -> state.withStatus("Error: " + error, RED),
            newState -> newState
        );
    }

    private Result<State> handleAdd(State state) {
        String title = prompt("Title");
        String desc = prompt("Description");

        return app.createTask(title, desc)
            .map(task -> state.withStatus("Created: " + task.title(), GREEN));
    }

    // ... more handlers ...
}
```

The TUI demonstrates:
- **Functional state management** - Immutable `State` record with transitions
- **Result handling** - Using `fold()` for clean error recovery
- **Adapter isolation** - All display logic in the adapter, none in `TaskApp`

---

## Part 9: The Same App as a REST API

The beauty of hexagonal architecture: add REST without changing `TaskApp`:

```java
// Hypothetical REST adapter using any framework
@RestController
@RequestMapping("/api/tasks")
public class TaskRestAdapter {

    private final TaskApp app;

    public TaskRestAdapter(TaskApp app) {
        this.app = app;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest req) {
        return app.createTask(req.title(), req.description())
            .fold(
                error -> ResponseEntity.badRequest().body(error),
                task -> ResponseEntity.created(uri(task.id())).body(task)
            );
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startTask(@PathVariable String id) {
        return app.startTask(id)
            .fold(
                error -> ResponseEntity.badRequest().body(error),
                ResponseEntity::ok
            );
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable String id) {
        return app.completeTask(id)
            .fold(
                error -> ResponseEntity.badRequest().body(error),
                ResponseEntity::ok
            );
    }

    @GetMapping
    public List<Task> listTasks() {
        return app.listTasks();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        return app.deleteTask(id)
            .fold(
                error -> ResponseEntity.notFound().build(),
                success -> ResponseEntity.noContent().build()
            );
    }
}
```

Same `TaskApp`, different interface. The business logic stays untouched.

---

## Part 10: Testing

HexaFun provides a testing DSL that integrates with the type-safe keys:

```java
@Test
void shouldCreateTask() {
    TaskApp app = TaskApp.withInMemoryRepo();

    app.getApp().test(CREATE)
        .with(new CreateTask("Learn HexaFun", "Study the DSL"))
        .expectOk(task -> {
            assertEquals("Learn HexaFun", task.title());
            assertEquals(TaskStatus.TODO, task.status());
        });
}

@Test
void shouldFailOnEmptyTitle() {
    TaskApp app = TaskApp.withInMemoryRepo();

    app.getApp().test(CREATE)
        .with(new CreateTask("", "No title"))
        .expectFailure(error ->
            assertEquals("Title cannot be empty", error)
        );
}

@Test
void shouldChainValidators() {
    TaskApp app = TaskApp.withInMemoryRepo();
    String longTitle = "A".repeat(101);

    app.getApp().test(CREATE)
        .with(new CreateTask(longTitle, "Too long"))
        .expectFailure(error ->
            assertTrue(error.contains("100 characters"))
        );
}

@Test
void shouldProgressTaskThroughWorkflow() {
    TaskApp app = TaskApp.withInMemoryRepo();

    // Create
    Result<Task> created = app.createTask("Test", "Description");
    assertTrue(created.isSuccess());
    assertEquals(TaskStatus.TODO, created.get().status());

    // Start
    Result<Task> started = app.startTask(created.get().id());
    assertTrue(started.isSuccess());
    assertEquals(TaskStatus.DOING, started.get().status());

    // Complete
    Result<Task> completed = app.completeTask(started.get().id());
    assertTrue(completed.isSuccess());
    assertEquals(TaskStatus.DONE, completed.get().status());
}
```

---

## Architecture Summary

```
┌─────────────────────────────────────────────────────────────────┐
│                      DRIVING ADAPTERS                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │  Terminal UI │  │   REST API   │  │   CLI Args   │           │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘           │
└─────────┼─────────────────┼─────────────────┼───────────────────┘
          │                 │                 │
          └────────────────►│◄────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                        TaskApp (HexaApp)                        │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                      USE CASES                             │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │ │
│  │  │ CREATE  │ │  START  │ │COMPLETE │ │ DELETE  │  ...      │ │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘           │ │
│  │       │           │           │           │                │ │
│  │       └───────────┴───────────┴───────────┘                │ │
│  │                        │                                   │ │
│  │              ┌─────────▼─────────┐                         │ │
│  │              │   Domain Model    │                         │ │
│  │              │  Task, TaskStatus │                         │ │
│  │              └───────────────────┘                         │ │
│  └────────────────────────────────────────────────────────────┘ │
│                            │                                    │
│                   Output Port Interface                         │
│                   (TaskRepository)                              │
└────────────────────────────┼────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                     DRIVEN ADAPTERS                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │  In-Memory   │  │  PostgreSQL  │  │   MongoDB    │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Takeaways

1. **Domain First** - Start with immutable domain models and behavior
2. **Use Case Keys** - Type-safe identifiers prevent runtime errors
3. **Validate/Handle** - Clear separation of concerns in the DSL
4. **Port Registry** - Dependency injection without a framework
5. **Result Monad** - Functional error handling with `fold()`
6. **Adapter Freedom** - Add TUI, REST, CLI without touching business logic

---

## Running the Example

```bash
# From the hexafun-examples directory
mvn compile exec:java -Dexec.mainClass="com.guinetik.hexafun.examples.tasks.tui.TasksTUI"
```

The TUI adapts to your terminal width, showing a Kanban board on wide terminals
and a simple list on narrow ones.

---

## Next Steps

- Explore the [Fluent DSL](fluent.html) for more advanced patterns
- Check out the [Counter Example](/hexafun-examples/xref/com/guinetik/hexafun/examples/counter/CounterApp.html#CounterApp) for a simpler starting point
- Add your own adapters: GraphQL, WebSocket, message queues

HexaFun makes it easy to build clean, testable applications where business logic
stays isolated from infrastructure concerns. The hexagon protects your domain.
