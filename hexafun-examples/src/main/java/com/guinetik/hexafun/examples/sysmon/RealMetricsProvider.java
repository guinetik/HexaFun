package com.guinetik.hexafun.examples.sysmon;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

/**
 * Real implementation of MetricsProvider using JVM/OS metrics.
 *
 * <p>Uses Java's management beans to read actual system metrics.
 * Note: CPU load may return -1 if not available on the platform.</p>
 */
public class RealMetricsProvider implements MetricsProvider {

    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;

    public RealMetricsProvider() {
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
    }

    @Override
    public double getCpuUsage() {
        if (
            osBean instanceof com.sun.management.OperatingSystemMXBean sunBean
        ) {
            double load = sunBean.getCpuLoad();

            // First call returns -1, need to wait and retry
            if (load < 0) {
                try {
                    Thread.sleep(100); // Let it collect data
                    load = sunBean.getCpuLoad();
                } catch (InterruptedException ignored) {}
            }

            if (load >= 0) {
                return load * 100.0;
            }
        }
        return -1;
    }

    @Override
    public double getMemoryUsage() {
        if (
            osBean instanceof com.sun.management.OperatingSystemMXBean sunBean
        ) {
            long total = sunBean.getTotalMemorySize();
            long free = sunBean.getFreeMemorySize();
            if (total > 0) {
                return ((double) (total - free) / total) * 100.0;
            }
        }

        // Fallback: JVM heap usage (not system memory, but better than nothing)
        var heap = memoryBean.getHeapMemoryUsage();
        return ((double) heap.getUsed() / heap.getMax()) * 100.0;
    }

    @Override
    public double getDiskUsage() {
        // Get all roots and pick the main one
        File[] roots = File.listRoots();
        if (roots.length == 0) return -1;

        // Use first root (C:\ on Windows, / on Unix)
        File root = roots[0];
        long total = root.getTotalSpace();
        long free = root.getUsableSpace(); // usableSpace is more accurate than freeSpace

        if (total > 0) {
            return ((double) (total - free) / total) * 100.0;
        }
        return -1;
    }
}
