package com.guinetik.hexafun.examples.sysmon;

import static com.guinetik.hexafun.examples.tui.Ansi.*;

/**
 * Output format options for the System Monitor.
 *
 * <p>Each format represents a different adapter that transforms
 * {@link SystemMetrics} into a specific string representation.</p>
 */
public enum SysmonFormat {
    TUI("TUI", "Colorful gauges", CYAN),
    CLI("CLI", "Plain text", GREEN),
    JSON("JSON", "Machine readable", YELLOW),
    PROMETHEUS("Prometheus", "Metrics format", MAGENTA);

    final String label;
    final String desc;
    final String color;

    SysmonFormat(String label, String desc, String color) {
        this.label = label;
        this.desc = desc;
        this.color = color;
    }
}
