package com.guinetik.hexafun.examples.sysmon;

import java.util.function.Function;

import static com.guinetik.hexafun.examples.tui.Ansi.*;
import static com.guinetik.hexafun.examples.tui.Widgets.*;

/**
 * Output adapters for SystemMetrics - transforms domain data to presentation formats.
 *
 * <p>This is where the hexagonal architecture shines: same domain object,
 * four completely different presentations. The domain knows nothing about
 * how it will be displayed.</p>
 *
 * <h2>Available Formats</h2>
 * <ul>
 *   <li><b>TUI</b> - Colorful progress bars with warnings</li>
 *   <li><b>CLI</b> - Plain text for scripting</li>
 *   <li><b>JSON</b> - Machine-readable</li>
 *   <li><b>Prometheus</b> - Metrics exposition format</li>
 * </ul>
 */
public final class SysmonAdapters {

    private SysmonAdapters() {}

    private static final int BAR_WIDTH = 20;
    private static final int BOX_WIDTH = 36;

    // ═══════════════════════════════════════════════════════════════════
    //  TUI ADAPTER - Colorful gauges with box drawing
    // ═══════════════════════════════════════════════════════════════════

    /**
     * TUI format with colorful progress bars and warning indicators.
     *
     * <pre>
     * ┌─ System Monitor ─────────────────┐
     * │ CPU  [████████░░░░░░░░░░░░] 67%  │
     * │ MEM  [██████████░░░░░░░░░░] 52%  │
     * │ DISK [██████████████████░░] 91% ⚠│
     * └──────────────────────────────────┘
     * </pre>
     */
    public static final Function<SystemMetrics, String> TUI_ADAPTER = metrics -> {
        StringBuilder sb = new StringBuilder();
        String indent = "  ";

        // Header
        sb.append(indent).append(color(BOX_TOP_LEFT + "─ System Monitor " +
            repeat(BOX_HORIZONTAL, BOX_WIDTH - 19) + BOX_TOP_RIGHT, CYAN)).append("\n");

        // CPU gauge
        sb.append(indent).append(color(BOX_VERTICAL, CYAN))
          .append(gauge("CPU ", metrics.cpu(), metrics.cpuWarning()))
          .append(color(BOX_VERTICAL, CYAN)).append("\n");

        // Memory gauge
        sb.append(indent).append(color(BOX_VERTICAL, CYAN))
          .append(gauge("MEM ", metrics.memory(), metrics.memoryWarning()))
          .append(color(BOX_VERTICAL, CYAN)).append("\n");

        // Disk gauge
        sb.append(indent).append(color(BOX_VERTICAL, CYAN))
          .append(gauge("DISK", metrics.disk(), metrics.diskWarning()))
          .append(color(BOX_VERTICAL, CYAN)).append("\n");

        // Footer
        sb.append(indent).append(color(BOX_BOTTOM_LEFT + repeat(BOX_HORIZONTAL, BOX_WIDTH - 2) +
            BOX_BOTTOM_RIGHT, CYAN)).append("\n");

        return sb.toString();
    };

    /**
     * Build a single gauge line with color-coded bar.
     */
    private static String gauge(String label, double percent, boolean warning) {
        int filled = (int) ((BAR_WIDTH * percent) / 100);
        int empty = BAR_WIDTH - filled;

        String barColor = warning ? RED : GREEN;
        String warnIcon = warning ? color(" ⚠", YELLOW) : "  ";

        return " " + color(label, BOLD) + " " +
            color("[", DIM) +
            color(repeat(BLOCK_FULL, filled), barColor) +
            color(repeat(BLOCK_LIGHT, empty), BRIGHT_BLACK) +
            color("]", DIM) +
            color(String.format(" %3.0f%%", percent), warning ? RED : WHITE) +
            warnIcon + " ";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CLI ADAPTER - Plain text for scripting
    // ═══════════════════════════════════════════════════════════════════

    /**
     * CLI format - simple key: value pairs for shell scripts.
     *
     * <pre>
     * cpu: 67%
     * mem: 52%
     * disk: 91%
     * </pre>
     */
    public static final Function<SystemMetrics, String> CLI_ADAPTER = metrics ->
        String.format("cpu: %.0f%%\nmem: %.0f%%\ndisk: %.0f%%\n",
            metrics.cpu(), metrics.memory(), metrics.disk());

    // ═══════════════════════════════════════════════════════════════════
    //  JSON ADAPTER - Machine readable
    // ═══════════════════════════════════════════════════════════════════

    /**
     * JSON format for APIs and data interchange (pretty-printed).
     *
     * <pre>
     * {
     *   "cpu": 67.0,
     *   "memory": 52.0,
     *   "disk": 91.0,
     *   "warnings": ["disk"]
     * }
     * </pre>
     */
    public static final Function<SystemMetrics, String> JSON_ADAPTER = metrics -> {
        StringBuilder warnings = new StringBuilder("[");
        boolean first = true;
        if (metrics.cpuWarning()) {
            warnings.append("\"cpu\"");
            first = false;
        }
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

    // ═══════════════════════════════════════════════════════════════════
    //  PROMETHEUS ADAPTER - Metrics exposition format
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Prometheus exposition format for monitoring systems.
     *
     * <pre>
     * # HELP system_cpu_percent Current CPU usage percentage
     * # TYPE system_cpu_percent gauge
     * system_cpu_percent 67.0
     * # HELP system_memory_percent Current memory usage percentage
     * # TYPE system_memory_percent gauge
     * system_memory_percent 52.0
     * # HELP system_disk_percent Current disk usage percentage
     * # TYPE system_disk_percent gauge
     * system_disk_percent 91.0
     * </pre>
     */
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
}
