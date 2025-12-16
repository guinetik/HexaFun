package com.guinetik.hexafun.examples.sysmon;

import com.guinetik.hexafun.hexa.AdapterKey;
import com.guinetik.hexafun.hexa.UseCaseKey;

/**
 * Type-safe keys for the system monitor use cases and adapters.
 *
 * <p>Centralizing keys here provides:
 * <ul>
 *   <li>Single source of truth for all operations</li>
 *   <li>Compile-time type safety</li>
 *   <li>IDE discoverability</li>
 * </ul>
 */
public final class SysmonKeys {

    private SysmonKeys() {}

    // ═══════════════════════════════════════════════════════════════════
    //  USE CASE KEYS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get CPU usage. Input: Void (no input needed), Output: Double (percentage).
     */
    public static final UseCaseKey<Void, Double> GET_CPU =
        UseCaseKey.of("getCpu");

    /**
     * Get memory usage. Input: Void, Output: Double (percentage).
     */
    public static final UseCaseKey<Void, Double> GET_MEMORY =
        UseCaseKey.of("getMemory");

    /**
     * Get disk usage. Input: Void, Output: Double (percentage).
     */
    public static final UseCaseKey<Void, Double> GET_DISK =
        UseCaseKey.of("getDisk");

    /**
     * Get all metrics at once. Input: Void, Output: SystemMetrics.
     */
    public static final UseCaseKey<Void, SystemMetrics> GET_ALL =
        UseCaseKey.of("getAllMetrics");

    // ═══════════════════════════════════════════════════════════════════
    //  ADAPTER KEYS - Transform SystemMetrics to various output formats
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Adapt to TUI format - colorful progress bars with box drawing.
     */
    public static final AdapterKey<SystemMetrics, String> TO_TUI =
        AdapterKey.of("toTui");

    /**
     * Adapt to CLI format - plain text suitable for scripting.
     */
    public static final AdapterKey<SystemMetrics, String> TO_CLI =
        AdapterKey.of("toCli");

    /**
     * Adapt to JSON format - machine-readable output.
     */
    public static final AdapterKey<SystemMetrics, String> TO_JSON =
        AdapterKey.of("toJson");

    /**
     * Adapt to Prometheus format - metrics exposition format.
     */
    public static final AdapterKey<SystemMetrics, String> TO_PROMETHEUS =
        AdapterKey.of("toPrometheus");
}
