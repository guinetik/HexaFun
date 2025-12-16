package com.guinetik.hexafun.examples.sysmon;

import java.util.Random;

/**
 * Mock implementation of MetricsProvider for testing and demos.
 *
 * <p>Returns configurable or random values. Useful for:
 * <ul>
 *   <li>Unit testing handlers without OS dependencies</li>
 *   <li>Demos with controlled/interesting values</li>
 *   <li>Simulating warning conditions</li>
 * </ul>
 */
public class MockMetricsProvider implements MetricsProvider {

    private final double cpu;
    private final double memory;
    private final double disk;

    /**
     * Create with fixed values.
     */
    public MockMetricsProvider(double cpu, double memory, double disk) {
        this.cpu = cpu;
        this.memory = memory;
        this.disk = disk;
    }

    /**
     * Create with random values within realistic ranges.
     */
    public static MockMetricsProvider random() {
        Random r = new Random();
        return new MockMetricsProvider(
            20 + r.nextDouble() * 60,  // CPU: 20-80%
            30 + r.nextDouble() * 50,  // Memory: 30-80%
            40 + r.nextDouble() * 55   // Disk: 40-95%
        );
    }

    /**
     * Create with values designed to show warnings.
     */
    public static MockMetricsProvider withWarnings() {
        return new MockMetricsProvider(67.0, 52.0, 91.0);
    }

    /**
     * Create with all healthy values.
     */
    public static MockMetricsProvider healthy() {
        return new MockMetricsProvider(35.0, 48.0, 62.0);
    }

    @Override
    public double getCpuUsage() {
        return cpu;
    }

    @Override
    public double getMemoryUsage() {
        return memory;
    }

    @Override
    public double getDiskUsage() {
        return disk;
    }
}
