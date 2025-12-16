package com.guinetik.hexafun.examples.sysmon;

/**
 * Output port for retrieving system metrics.
 *
 * <p>This interface abstracts the source of system metrics, allowing
 * the domain to remain independent of how metrics are obtained.
 * Implementations can read from the real OS, return mock data, or
 * fetch from remote monitoring systems.</p>
 *
 * <h2>Hexagonal Architecture Role</h2>
 * <p>This is a <strong>driven port</strong> (output port) - the application
 * core calls out to infrastructure through this interface.</p>
 */
public interface MetricsProvider {

    /**
     * Get current CPU usage percentage.
     *
     * @return CPU usage as percentage (0-100)
     */
    double getCpuUsage();

    /**
     * Get current memory usage percentage.
     *
     * @return Memory usage as percentage (0-100)
     */
    double getMemoryUsage();

    /**
     * Get current disk usage percentage.
     *
     * @return Disk usage as percentage (0-100)
     */
    double getDiskUsage();

    /**
     * Get all system metrics at once.
     *
     * <p>Default implementation calls individual methods, but implementations
     * may override for efficiency (e.g., single OS call).</p>
     *
     * @return SystemMetrics containing all current values
     */
    default SystemMetrics getAllMetrics() {
        return new SystemMetrics(getCpuUsage(), getMemoryUsage(), getDiskUsage());
    }
}
