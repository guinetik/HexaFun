# Adapters: One Domain, Many Faces

This guide shows how HexaFun's adapter pattern lets you transform domain objects
into multiple output formats without changing your business logic. We'll build a
system monitor that outputs the same metrics as colorful TUI gauges, plain text,
JSON, and Prometheus format.

<script src="https://asciinema.org/a/762262.js" id="asciicast-762262" async="true"></script>

---

## What Are Adapters?

In hexagonal architecture, **adapters** bridge your domain and the outside world:

| Type | Direction | Example |
|------|-----------|---------|
| **Driving Adapter** | Outside → Domain | REST controller, CLI, TUI |
| **Driven Adapter** | Domain → Outside | Database repository, HTTP client |
| **Output Adapter** | Domain → Presentation | JSON formatter, CSV exporter |

HexaFun's `AdapterKey` system focuses on **output adapters** - pure functions that
transform domain objects into presentation formats. The domain stays clean;
adapters handle the mess.

---

## The Problem

Imagine a monitoring system that tracks CPU, memory, and disk usage. Different
consumers need different formats:

- **Operators**: Colorful TUI with progress bars and warning indicators
- **Scripts**: Plain `key: value` pairs for parsing
- **APIs**: JSON for REST endpoints
- **Monitoring**: Prometheus exposition format for Grafana

Without adapters, you'd either:
1. Bloat your domain with formatting logic
2. Duplicate transformation code everywhere
3. Create tight coupling between domain and presentation

---

## Part 1: The Domain Model

Start with a clean domain record that knows nothing about presentation:

```java
public record SystemMetrics(double cpu, double memory, double disk) {

    public static final double WARNING_THRESHOLD = 80.0;

    public boolean cpuWarning() {
        return cpu >= WARNING_THRESHOLD;
    }

    public boolean memoryWarning() {
        return memory >= WARNING_THRESHOLD;
    }

    public boolean diskWarning() {
        return disk >= WARNING_THRESHOLD;
    }

    public boolean hasWarnings() {
        return cpuWarning() || memoryWarning() || diskWarning();
    }
}
```

Key design choices:
- **Pure data** - No formatting, no I/O
- **Domain behavior** - `WARNING_THRESHOLD` is business logic, not presentation
- **Immutable record** - Thread-safe, predictable

---

## Part 2: Type-Safe Adapter Keys

Just like `UseCaseKey` for use cases, `AdapterKey` provides type-safe identifiers:

```java
public final class SysmonKeys {

    // Use Case Keys - what the app does
    public static final UseCaseKey<Void, SystemMetrics> GET_ALL =
        UseCaseKey.of("getAllMetrics");

    // Adapter Keys - how output is formatted
    public static final AdapterKey<SystemMetrics, String> TO_TUI =
        AdapterKey.of("toTui");

    public static final AdapterKey<SystemMetrics, String> TO_CLI =
        AdapterKey.of("toCli");

    public static final AdapterKey<SystemMetrics, String> TO_JSON =
        AdapterKey.of("toJson");

    public static final AdapterKey<SystemMetrics, String> TO_PROMETHEUS =
        AdapterKey.of("toPrometheus");
}
```

Each `AdapterKey<I, O>` captures:
- **Input type** - The domain object to transform (`SystemMetrics`)
- **Output type** - The result format (`String` in this case)
- **Name** - For debugging and logging

---

## Part 3: Implementing Adapters

Adapters are just `Function<I, O>` - pure transformations:

### TUI Adapter - Colorful Gauges

```java
public static final Function<SystemMetrics, String> TUI_ADAPTER = metrics -> {
    StringBuilder sb = new StringBuilder();

    // Header with box drawing
    sb.append(color("┌─ System Monitor ─────────────────┐", CYAN)).append("\n");

    // CPU gauge
    sb.append(color("│", CYAN))
      .append(gauge("CPU ", metrics.cpu(), metrics.cpuWarning()))
      .append(color("│", CYAN)).append("\n");

    // Memory gauge
    sb.append(color("│", CYAN))
      .append(gauge("MEM ", metrics.memory(), metrics.memoryWarning()))
      .append(color("│", CYAN)).append("\n");

    // Disk gauge
    sb.append(color("│", CYAN))
      .append(gauge("DISK", metrics.disk(), metrics.diskWarning()))
      .append(color("│", CYAN)).append("\n");

    sb.append(color("└──────────────────────────────────┘", CYAN)).append("\n");

    return sb.toString();
};

private static String gauge(String label, double percent, boolean warning) {
    int filled = (int) ((20 * percent) / 100);
    int empty = 20 - filled;

    String barColor = warning ? RED : GREEN;
    String warnIcon = warning ? color(" ⚠", YELLOW) : "  ";

    return " " + color(label, BOLD) + " " +
        color("[", DIM) +
        color("█".repeat(filled), barColor) +
        color("░".repeat(empty), BRIGHT_BLACK) +
        color("]", DIM) +
        color(String.format(" %3.0f%%", percent), warning ? RED : WHITE) +
        warnIcon + " ";
}
```

Output:
```
┌─ System Monitor ─────────────────┐
│ CPU  [████████░░░░░░░░░░░░] 67%  │
│ MEM  [██████████░░░░░░░░░░] 52%  │
│ DISK [██████████████████░░] 91% ⚠│
└──────────────────────────────────┘
```

### CLI Adapter - Plain Text

```java
public static final Function<SystemMetrics, String> CLI_ADAPTER = metrics ->
    String.format("cpu: %.0f%%\nmem: %.0f%%\ndisk: %.0f%%\n",
        metrics.cpu(), metrics.memory(), metrics.disk());
```

Output:
```
cpu: 67%
mem: 52%
disk: 91%
```

### JSON Adapter - Machine Readable

```java
public static final Function<SystemMetrics, String> JSON_ADAPTER = metrics -> {
    StringBuilder warnings = new StringBuilder("[");
    boolean first = true;
    if (metrics.cpuWarning()) { warnings.append("\"cpu\""); first = false; }
    if (metrics.memoryWarning()) {
        if (!first) warnings.append(", ");
        warnings.append("\"memory\"");
        first = false;
    }
    if (metrics.diskWarning()) {
        if (!first) warnings.append(", ");
        warnings.append("\"disk\"");
    }
    warnings.append("]");

    return String.format(
        "{\n  \"cpu\": %.1f,\n  \"memory\": %.1f,\n  \"disk\": %.1f,\n  \"warnings\": %s\n}\n",
        metrics.cpu(), metrics.memory(), metrics.disk(), warnings
    );
};
```

Output:
```json
{
  "cpu": 67.0,
  "memory": 52.0,
  "disk": 91.0,
  "warnings": ["disk"]
}
```

### Prometheus Adapter - Metrics Format

```java
public static final Function<SystemMetrics, String> PROMETHEUS_ADAPTER = metrics -> {
    StringBuilder sb = new StringBuilder();

    sb.append("# HELP system_cpu_percent Current CPU usage percentage\n");
    sb.append("# TYPE system_cpu_percent gauge\n");
    sb.append(String.format("system_cpu_percent %.1f\n", metrics.cpu()));

    sb.append("# HELP system_memory_percent Current memory usage percentage\n");
    sb.append("# TYPE system_memory_percent gauge\n");
    sb.append(String.format("system_memory_percent %.1f\n", metrics.memory()));

    sb.append("# HELP system_disk_percent Current disk usage percentage\n");
    sb.append("# TYPE system_disk_percent gauge\n");
    sb.append(String.format("system_disk_percent %.1f\n", metrics.disk()));

    return sb.toString();
};
```

Output:
```
# HELP system_cpu_percent Current CPU usage percentage
# TYPE system_cpu_percent gauge
system_cpu_percent 67.0
# HELP system_memory_percent Current memory usage percentage
# TYPE system_memory_percent gauge
system_memory_percent 52.0
# HELP system_disk_percent Current disk usage percentage
# TYPE system_disk_percent gauge
system_disk_percent 91.0
```

---

## Part 4: The Output Port

The domain needs metrics from somewhere. Define an output port:

```java
public interface MetricsProvider {

    double getCpuUsage();

    double getMemoryUsage();

    double getDiskUsage();

    default SystemMetrics getAllMetrics() {
        return new SystemMetrics(getCpuUsage(), getMemoryUsage(), getDiskUsage());
    }
}
```

This is a **driven port** - the application core calls out through this interface.
Implementations can read from the real OS, return mock data, or fetch remotely.

---

## Part 5: Use Case Handlers

Handlers connect use cases to ports using `UseCaseHandler`:

```java
public static class GetAllMetricsHandler extends UseCaseHandler<Void, SystemMetrics> {

    public GetAllMetricsHandler(HexaApp app) {
        super(app);
    }

    @Override
    public SystemMetrics apply(Void input) {
        return port(MetricsProvider.class).getAllMetrics();
    }
}
```

The `UseCaseHandler` base class provides:
- Access to the port registry via `port(Class<T>)`
- Clean separation between orchestration and infrastructure

---

## Part 6: Wiring It Together

Now wire use cases and adapters into a `HexaApp`:

```java
public final class SysmonApp {

    public static HexaApp createApp(MetricsProvider provider) {
        HexaApp app = HexaApp.create().port(MetricsProvider.class, provider);

        // Register use case handlers
        app.withUseCase(GET_CPU, new GetCpuHandler(app))
           .withUseCase(GET_MEMORY, new GetMemoryHandler(app))
           .withUseCase(GET_DISK, new GetDiskHandler(app))
           .withUseCase(GET_ALL, new GetAllMetricsHandler(app));

        // Register output adapters
        app.withAdapter(TO_TUI, TUI_ADAPTER)
           .withAdapter(TO_CLI, CLI_ADAPTER)
           .withAdapter(TO_JSON, JSON_ADAPTER)
           .withAdapter(TO_PROMETHEUS, PROMETHEUS_ADAPTER);

        return app;
    }
}
```

Key patterns:
- **Port registration** - `provider` injected via `.port()`
- **Use case registration** - Handlers wired with `.withUseCase()`
- **Adapter registration** - Formatters wired with `.withAdapter()`

---

## Part 7: Using Adapters

Invoke adapters with type-safe keys:

```java
// Get domain data
SystemMetrics metrics = app.invoke(GET_ALL, null);

// Transform to different formats
String tui = app.adapt(TO_TUI, metrics);
String cli = app.adapt(TO_CLI, metrics);
String json = app.adapt(TO_JSON, metrics);
String prometheus = app.adapt(TO_PROMETHEUS, metrics);
```

Or chain them in one flow:

```java
// Invoke use case and adapt in sequence
SystemMetrics metrics = app.invoke(GET_ALL, null);
String output = app.adapt(currentFormat, metrics);
System.out.print(output);
```

---

## Part 8: The TUI (Driving Adapter)

The TUI is a **driving adapter** - it drives the application from user input:

```java
public class SysmonTUI {

    private final HexaApp app;

    public SysmonTUI() {
        this.app = SysmonApp.createApp(new OshiMetricsProvider());
    }

    public void run() {
        SysmonState state = SysmonState.initial(app);

        while (state.running()) {
            render(state);
            int key = readKeyWithTimeout(2000);

            if (key == -1) {
                state = state.refresh();  // Auto-refresh on timeout
            } else {
                state = processKey(state, key);
            }
        }
    }

    private SysmonState processKey(SysmonState state, int key) {
        return switch (Character.toLowerCase((char) key)) {
            case 'q', 3 -> state.stop();
            case '1' -> state.withFormat(SysmonFormat.TUI);
            case '2' -> state.withFormat(SysmonFormat.CLI);
            case '3' -> state.withFormat(SysmonFormat.JSON);
            case '4' -> state.withFormat(SysmonFormat.PROMETHEUS);
            case 'r', ' ', '\n' -> state.refresh();
            default -> state;
        };
    }
}
```

The TUI:
- Creates the app with a real metrics provider
- Manages immutable state for the current format
- Switches adapters based on user input (1-4 keys)
- Auto-refreshes every 2 seconds

---

## Part 9: Adding New Adapters

Need XML output? Add an adapter without touching existing code:

```java
// 1. Define the key
public static final AdapterKey<SystemMetrics, String> TO_XML =
    AdapterKey.of("toXml");

// 2. Implement the adapter
public static final Function<SystemMetrics, String> XML_ADAPTER = metrics ->
    String.format("""
        <?xml version="1.0"?>
        <metrics>
          <cpu>%.1f</cpu>
          <memory>%.1f</memory>
          <disk>%.1f</disk>
        </metrics>
        """,
        metrics.cpu(), metrics.memory(), metrics.disk()
    );

// 3. Register it
app.withAdapter(TO_XML, XML_ADAPTER);

// 4. Use it
String xml = app.adapt(TO_XML, metrics);
```

No changes to:
- Domain model (`SystemMetrics`)
- Use cases (handlers)
- Existing adapters
- The TUI (just add case '5' for XML)

---

## Architecture Summary

```
┌─────────────────────────────────────────────────────────────────┐
│                      DRIVING ADAPTERS                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │   TUI (1-4)  │  │   REST API   │  │   CLI Args   │           │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘           │
└─────────┼─────────────────┼─────────────────┼───────────────────┘
          │                 │                 │
          └────────────────►│◄────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                     SysmonApp (HexaApp)                         │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                     USE CASES                              │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │ │
│  │  │ GET_CPU │ │ GET_MEM │ │GET_DISK │ │ GET_ALL │           │ │
│  │  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘           │ │
│  │       └───────────┴───────────┴───────────┘                │ │
│  │                       │                                    │ │
│  │             ┌─────────▼─────────┐                          │ │
│  │             │  SystemMetrics    │                          │ │
│  │             │  (Domain Model)   │                          │ │
│  │             └─────────┬─────────┘                          │ │
│  │                       │                                    │ │
│  │  ┌────────────────────▼────────────────────┐               │ │
│  │  │            OUTPUT ADAPTERS              │               │ │
│  │  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌────────┐  │               │ │
│  │  │  │ TUI  │ │ CLI  │ │ JSON │ │PROMETH │  │               │ │
│  │  │  └──────┘ └──────┘ └──────┘ └────────┘  │               │ │
│  │  └─────────────────────────────────────────┘               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                            │                                    │
│                   Output Port Interface                         │
│                   (MetricsProvider)                             │
└────────────────────────────┼────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                     DRIVEN ADAPTERS                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │OshiMetrics   │  │ MockMetrics  │  │RemoteMetrics │           │
│  │(cross-plat)  │  │ (Testing)    │  │ (HTTP API)   │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Takeaways

1. **Adapters are pure functions** - `Function<I, O>` with no side effects
2. **Type-safe keys** - `AdapterKey<I, O>` prevents runtime type errors
3. **Domain stays clean** - No formatting logic in `SystemMetrics`
4. **Open for extension** - Add adapters without modifying existing code
5. **Testable** - Adapters are trivial to unit test (input → expected output)
6. **Composable** - Chain `invoke()` and `adapt()` for clean data flows

---

## Running the Example

```bash
# From the hexafun-examples directory
mvn compile exec:java -Dexec.mainClass="com.guinetik.hexafun.examples.sysmon.SysmonTUI"
```

Press 1-4 to switch formats, 'r' to refresh, 'q' to quit.

---

## Next Steps

- Read the [Tutorial](tutorial.html) for a complete CRUD application
- Explore the [Fluent DSL](fluent.html) for use case composition
- Check the [Javadoc](apidocs/index.html) for API details
