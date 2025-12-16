package com.guinetik.hexafun.examples.sysmon;

/**
 * Domain record representing system metrics.
 *
 * <p>This is the core domain object that flows through the hexagonal architecture.
 * Different adapters transform this same data into various output formats.</p>
 *
 * @param cpu CPU usage percentage (0-100)
 * @param memory Memory usage percentage (0-100)
 * @param disk Disk usage percentage (0-100)
 */
public record SystemMetrics(double cpu, double memory, double disk) {

    /**
     * Threshold for warning indicators.
     */
    public static final double WARNING_THRESHOLD = 80.0;

    /**
     * Check if CPU usage is above warning threshold.
     */
    public boolean cpuWarning() {
        return cpu >= WARNING_THRESHOLD;
    }

    /**
     * Check if memory usage is above warning threshold.
     */
    public boolean memoryWarning() {
        return memory >= WARNING_THRESHOLD;
    }

    /**
     * Check if disk usage is above warning threshold.
     */
    public boolean diskWarning() {
        return disk >= WARNING_THRESHOLD;
    }

    /**
     * Check if any metric is above warning threshold.
     */
    public boolean hasWarnings() {
        return cpuWarning() || memoryWarning() || diskWarning();
    }
}
