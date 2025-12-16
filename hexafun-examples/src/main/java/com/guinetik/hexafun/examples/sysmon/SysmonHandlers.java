package com.guinetik.hexafun.examples.sysmon;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.hexa.UseCaseHandler;

/**
 * Use case handlers for system monitoring operations.
 *
 * <p>Each handler extends {@link UseCaseHandler} to get port access,
 * then delegates to the {@link MetricsProvider} port for actual data.</p>
 *
 * <h2>Architecture</h2>
 * <pre>
 * [Handler] --uses--> [MetricsProvider port] --impl--> [Mock/Real Provider]
 * </pre>
 */
public final class SysmonHandlers {

    private SysmonHandlers() {}

    /**
     * Handler for GET_CPU use case.
     */
    public static class GetCpuHandler extends UseCaseHandler<Void, Double> {

        public GetCpuHandler(HexaApp app) {
            super(app);
        }

        @Override
        public Double apply(Void input) {
            return port(MetricsProvider.class).getCpuUsage();
        }
    }

    /**
     * Handler for GET_MEMORY use case.
     */
    public static class GetMemoryHandler extends UseCaseHandler<Void, Double> {

        public GetMemoryHandler(HexaApp app) {
            super(app);
        }

        @Override
        public Double apply(Void input) {
            return port(MetricsProvider.class).getMemoryUsage();
        }
    }

    /**
     * Handler for GET_DISK use case.
     */
    public static class GetDiskHandler extends UseCaseHandler<Void, Double> {

        public GetDiskHandler(HexaApp app) {
            super(app);
        }

        @Override
        public Double apply(Void input) {
            return port(MetricsProvider.class).getDiskUsage();
        }
    }

    /**
     * Handler for GET_ALL use case.
     *
     * <p>Returns a complete SystemMetrics snapshot.</p>
     */
    public static class GetAllMetricsHandler extends UseCaseHandler<Void, SystemMetrics> {

        public GetAllMetricsHandler(HexaApp app) {
            super(app);
        }

        @Override
        public SystemMetrics apply(Void input) {
            return port(MetricsProvider.class).getAllMetrics();
        }
    }
}
