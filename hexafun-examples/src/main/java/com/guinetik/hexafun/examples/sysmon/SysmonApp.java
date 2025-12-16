package com.guinetik.hexafun.examples.sysmon;

import static com.guinetik.hexafun.examples.sysmon.SysmonAdapters.*;
import static com.guinetik.hexafun.examples.sysmon.SysmonKeys.*;

import com.guinetik.hexafun.HexaApp;

/**
 * Factory for creating the System Monitor HexaApp instance.
 *
 * <p>This class wires together:
 * <ul>
 *   <li>Use cases (handlers) for fetching metrics</li>
 *   <li>Adapters for transforming metrics to various output formats</li>
 *   <li>The MetricsProvider port for infrastructure access</li>
 * </ul>
 *
 * <h2>Hexagonal Architecture</h2>
 * <pre>
 * [TUI] --invokes--> [UseCase] --uses port--> [MetricsProvider]
 *                        |
 *                   [Adapter] --transforms--> [Output Format]
 * </pre>
 */
public final class SysmonApp {

    private SysmonApp() {}

    /**
     * Create a configured HexaApp for system monitoring.
     *
     * @param provider The MetricsProvider implementation to use
     * @return Configured HexaApp with all use cases and adapters registered
     */
    public static HexaApp createApp(MetricsProvider provider) {
        HexaApp app = HexaApp.create().port(MetricsProvider.class, provider);

        // Register use case handlers
        app
            .withUseCase(GET_CPU, new SysmonHandlers.GetCpuHandler(app))
            .withUseCase(GET_MEMORY, new SysmonHandlers.GetMemoryHandler(app))
            .withUseCase(GET_DISK, new SysmonHandlers.GetDiskHandler(app))
            .withUseCase(GET_ALL, new SysmonHandlers.GetAllMetricsHandler(app));

        // Register output adapters
        app
            .withAdapter(TO_TUI, TUI_ADAPTER)
            .withAdapter(TO_CLI, CLI_ADAPTER)
            .withAdapter(TO_JSON, JSON_ADAPTER)
            .withAdapter(TO_PROMETHEUS, PROMETHEUS_ADAPTER);

        return app;
    }
}
