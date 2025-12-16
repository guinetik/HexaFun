package com.guinetik.hexafun.examples.sysmon;

import static com.guinetik.hexafun.examples.tui.Ansi.*;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.examples.tui.View;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * System Monitor TUI - interactive demo of UseCaseHandler and AdapterKey patterns.
 *
 * <p>Demonstrates the adapter pattern: same domain data ({@link SystemMetrics}),
 * four completely different presentations selected via menu.</p>
 *
 * <h2>Architecture</h2>
 * <pre>
 * [SysmonTUI] --renders--> [SysmonView] --uses--> [SysmonState]
 *                                                      |
 *                                                 [HexaApp]
 *                                                /         \
 *                                        [UseCases]    [Adapters]
 * </pre>
 *
 * <p>Run with: {@code java -cp hexafun-examples/target/hexafun-examples-*.jar
 * com.guinetik.hexafun.examples.sysmon.SysmonTUI}</p>
 */
public class SysmonTUI {

    // ═══════════════════════════════════════════════════════════════════
    //  RUNTIME
    // ═══════════════════════════════════════════════════════════════════

    private static final int REFRESH_INTERVAL_MS = 2000;

    private final View<SysmonState> screen;
    private final HexaApp app;
    private InputStream ttyInput;

    public SysmonTUI() {
        this.app = SysmonApp.createApp(new RealMetricsProvider());
        this.screen = SysmonView.screen();
    }

    private void render(SysmonState state) {
        System.out.print(screen.apply(state));
        System.out.flush();
    }

    /**
     * Read a single keypress with timeout.
     * Reads directly from /dev/tty for immediate response.
     */
    private int readKeyWithTimeout(int timeoutMs) {
        try {
            long deadline = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < deadline) {
                if (ttyInput.available() > 0) {
                    return ttyInput.read();
                }
                Thread.sleep(50);
            }
        } catch (IOException | InterruptedException ignored) {}
        return -1; // Timeout
    }

    /**
     * Set terminal to raw mode and enter alternate screen buffer.
     */
    private void setRawMode() {
        // Enter alternate screen buffer (doesn't pollute scrollback)
        System.out.print(ALT_SCREEN_ON + HIDE_CURSOR);
        System.out.flush();
        try {
            new ProcessBuilder("stty", "-icanon", "-echo")
                .inheritIO()
                .start()
                .waitFor();
        } catch (Exception ignored) {}
    }

    /**
     * Restore terminal to normal mode and exit alternate screen.
     */
    private void restoreTerminal() {
        // Exit alternate screen buffer, show cursor
        System.out.print(SHOW_CURSOR + ALT_SCREEN_OFF);
        System.out.flush();
        try {
            new ProcessBuilder("stty", "sane").inheritIO().start().waitFor();
        } catch (Exception ignored) {}
        try {
            if (ttyInput != null) ttyInput.close();
        } catch (Exception ignored) {}
    }

    /**
     * Main run loop - renders state, waits for input, processes transitions.
     */
    public void run() {
        SysmonState state = SysmonState.initial(app);

        // Open /dev/tty for direct keyboard input
        try {
            ttyInput = new FileInputStream("/dev/tty");
        } catch (Exception e) {
            // Fallback to System.in if /dev/tty not available
            ttyInput = System.in;
        }

        // Set up raw mode and ensure cleanup
        setRawMode();
        Runtime.getRuntime().addShutdownHook(new Thread(this::restoreTerminal));

        try {
            while (state.running()) {
                render(state);
                int key = readKeyWithTimeout(REFRESH_INTERVAL_MS);

                if (key == -1) {
                    // Timeout - auto refresh
                    state = state.refresh();
                } else {
                    state = processKey(state, key);
                }
            }
            render(state);
        } finally {
            restoreTerminal();
        }
    }

    /**
     * Process a keypress and return new state.
     */
    private SysmonState processKey(SysmonState state, int key) {
        char c = Character.toLowerCase((char) key);

        return switch (c) {
            case 'q', 3 -> state.stop(); // 'q' or Ctrl+C
            case '1' -> state.withFormat(SysmonFormat.TUI);
            case '2' -> state.withFormat(SysmonFormat.CLI);
            case '3' -> state.withFormat(SysmonFormat.JSON);
            case '4' -> state.withFormat(SysmonFormat.PROMETHEUS);
            case 'r', ' ', '\n' -> state.refresh();
            default -> state; // Ignore unknown keys
        };
    }

    // ═══════════════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        new SysmonTUI().run();
    }
}
