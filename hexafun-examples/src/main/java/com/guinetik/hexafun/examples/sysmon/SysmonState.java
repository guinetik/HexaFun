package com.guinetik.hexafun.examples.sysmon;

import static com.guinetik.hexafun.examples.sysmon.SysmonKeys.*;
import static com.guinetik.hexafun.examples.tui.Ansi.*;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.examples.tui.HexaTerminal;

/**
 * Immutable state for the System Monitor TUI.
 *
 * <p>This record holds all state needed to render the UI and process
 * user input. State transitions produce new instances rather than
 * mutating, enabling a functional TUI pattern.</p>
 *
 * <h2>State Transitions</h2>
 * <ul>
 *   <li>{@link #withFormat(SysmonFormat)} - Switch output format</li>
 *   <li>{@link #withStatus(String, String)} - Show status message</li>
 *   <li>{@link #refresh()} - Recalculate metrics</li>
 *   <li>{@link #stop()} - Signal exit</li>
 * </ul>
 *
 * @param app The HexaApp instance for invoking use cases and adapters
 * @param format Current output format selection
 * @param width Terminal width for responsive layout
 * @param status Status message to display
 * @param statusColor ANSI color for status message
 * @param running Whether the TUI should continue running
 */
public record SysmonState(
    HexaApp app,
    SysmonFormat format,
    int width,
    String status,
    String statusColor,
    boolean running
) {
    private static final int MIN_WIDTH = 50;

    /**
     * Create initial state with default values.
     *
     * @param app The configured HexaApp instance
     * @return Initial state ready for rendering
     */
    public static SysmonState initial(HexaApp app) {
        return new SysmonState(app, SysmonFormat.TUI, detectWidth(), "", GREEN, true);
    }

    /**
     * Transition to a new output format.
     *
     * @param f The new format to use
     * @return New state with format changed and status message
     */
    public SysmonState withFormat(SysmonFormat f) {
        return new SysmonState(
            app,
            f,
            width,
            "Switched to " + f.label,
            f.color,
            running
        );
    }

    /**
     * Set a status message.
     *
     * @param msg The message to display
     * @param color ANSI color for the message
     * @return New state with status updated
     */
    public SysmonState withStatus(String msg, String color) {
        return new SysmonState(app, format, width, msg, color, running);
    }

    /**
     * Signal that the TUI should stop.
     *
     * @return New state with running=false
     */
    public SysmonState stop() {
        return new SysmonState(app, format, width, "Goodbye!", CYAN, false);
    }

    /**
     * Refresh state, re-detecting terminal width.
     *
     * @return New state with cleared status and updated width
     */
    public SysmonState refresh() {
        return new SysmonState(app, format, detectWidth(), "", GREEN, running);
    }

    /**
     * Get current system metrics by invoking the GET_ALL use case.
     *
     * @return Current SystemMetrics from the provider
     */
    public SystemMetrics metrics() {
        return app.invoke(GET_ALL, null);
    }

    private static int detectWidth() {
        return HexaTerminal.detectWidth(MIN_WIDTH, 80);
    }
}
