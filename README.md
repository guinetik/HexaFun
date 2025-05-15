# HexaFun 🧹

> A functional, declarative Java framework for building Hexagonal Architecture apps with serious swagg.

Built for JDK 17+ with a clean DSL that puts your **use cases** front and center.
Forget the ceremony. Focus on composing behavior.

---

## 🔷 What is HexaFun?

HexaFun brings **Hexagonal Architecture** into the functional age — with minimal boilerplate and maximum clarity.

* 🚪 Define use cases with `.from().to()` pipelines
* 🧠 Pure business logic, no frameworks leaking in
* 🔌 Plug in real adapters (HTTP, DB, Messaging) when *you* want
* 💡 Composable, testable, functional-by-design

---

## 🧪 Example Application

A complete Todo app example is included in the `com.guinetik.hexafun.examples.todo` package. It demonstrates:

- Domain model (Todo)
- Use cases (create, complete, list)
- Input validation
- Repository interface
- In-memory repository implementation
- Functional error handling

Run the TodoAppDemo class to see it in action!

---

## 🧱 Architecture Overview

```
                     ┌────────────────────────┐
                     │   Driving Adapter      │  (REST / CLI / Events)
                     └────────▲───────────────┘
                              │
                        Input Port (UseCase<I, O>)
                              │
      ┌─────────────────────────────────────────────────────────────┐
      │                                                             │
      │             ┌──────────────┐     ┌───────────────────┐      │
      │             │  Use Cases   │◄──►│   Domain Model     │      │
      │             └──────────────┘     └───────────────────┘      │
      │                                                             │
      └──────────────▲──────────────────────────────────────────────┘
                     │              │
              Output Port     Output Port
               (e.g. DB)        (e.g. Email)
                     │              │
                 ┌───▼────┐     ┌───▼──────┐
                 │ Adapter│     │ Adapter  │
                 └────────┘     └──────────┘

```

---

## 🚀 Getting Started

### 1. Define a Use Case

```java
HexaApp app = HexaFun.dsl()
    .useCase("createTask")
        .from(title -> validate(title))              // validate input
        .to(task -> repo.save(task))                 // business logic
        .and()
    .useCase("listTasks")
        .to(unused -> repo.findAll())                // no validation
        .and()
    .build();
```

### 2. Invoke it from anywhere

```java
Result<Task> result = app.invoke("createTask", "Buy milk");
List<Task> tasks = app.invoke("listTasks", null);
```

---

## ✅ Supported Concepts

| Concept              | How HexaFun Supports It         |
| -------------------- | ------------------------------- |
| Use Cases            | `UseCase<I, O>` interface + DSL |
| Validation           | `Result<T>` and `.from()` step  |
| Functional Pipelines | Chain `.from(...).to(...)`      |
| Input Ports          | DSL exposes use cases           |
| Output Ports         | (Coming soon!) Bind interfaces  |
| Adapters             | (Coming soon!) HTTP, Kafka, CLI |
| Testing              | (Planned) Declarative test API  |

---

## 🛠 DSL Overview

```java
HexaFun.dsl()
    .useCase("name")
        .from(input -> Result.ok(...))      // optional
        .to(validated -> Result.ok(...))
        .and()
    .useCase("other")
        .to(input -> ...)
    .build();
```

---

## 🤪 Test Mode

```java
app.test("createTask")
   .with("Study HexaFun")
   .expectOk(task -> assertFalse(task.done()));
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

* `com.guinetik.hexafun.hexa` – Hexagonal contracts (`UseCase`, `InputPort`, etc)
* `com.guinetik.hexafun.fun` – Functional primitives (`Result`, etc)
* `com.guinetik.hexafun` – Core DSL + `HexaApp`
* `com.guinetik.hexafun.testing` – Testing framework
* `com.guinetik.hexafun.examples` – Example applications
