package com.guinetik.hexafun.examples.sysmon;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSFileStore;

import java.util.List;

/**
 * Cross-platform metrics provider using OSHI library.
 *
 * <p>OSHI (Operating System and Hardware Information) provides native
 * access to system metrics on Windows, Linux, macOS, and other platforms
 * without JNI dependencies.</p>
 *
 * <p>This implementation includes WSL compatibility fixes.</p>
 */
public class OshiMetricsProvider implements MetricsProvider {

    private final SystemInfo si = new SystemInfo();
    private final CentralProcessor cpu = si.getHardware().getProcessor();
    private final GlobalMemory memory = si.getHardware().getMemory();
    private long[] prevTicks;

    public OshiMetricsProvider() {
        // Prime the CPU tick counter - first reading needs baseline
        prevTicks = cpu.getSystemCpuLoadTicks();
        try {
            Thread.sleep(100); // Brief pause to get meaningful delta
        } catch (InterruptedException ignored) {}
        prevTicks = cpu.getSystemCpuLoadTicks();
    }

    @Override
    public double getCpuUsage() {
        double load = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = cpu.getSystemCpuLoadTicks();
        return Math.max(0, Math.min(100, load)); // Clamp to 0-100
    }

    @Override
    public double getMemoryUsage() {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        if (total <= 0) return 0;
        return ((double) (total - available) / total) * 100.0;
    }

    @Override
    public double getDiskUsage() {
        var fs = si.getOperatingSystem().getFileSystem();
        List<OSFileStore> stores = fs.getFileStores();
        if (stores.isEmpty()) return 0;

        // Find the best disk: prefer root "/" or "C:" over virtual mounts
        OSFileStore best = stores.get(0);
        long bestSize = 0;

        for (OSFileStore store : stores) {
            String mount = store.getMount();
            long total = store.getTotalSpace();

            // Skip virtual/empty filesystems
            if (total <= 0) continue;

            // Prefer root mounts
            if ("/".equals(mount) || mount.matches("[A-Z]:\\\\")) {
                best = store;
                break;
            }

            // Otherwise pick the largest real filesystem
            if (total > bestSize && !mount.startsWith("/snap") && !mount.startsWith("/boot")) {
                best = store;
                bestSize = total;
            }
        }

        long total = best.getTotalSpace();
        long usable = best.getUsableSpace();
        if (total <= 0) return 0;
        return ((double) (total - usable) / total) * 100.0;
    }
}
