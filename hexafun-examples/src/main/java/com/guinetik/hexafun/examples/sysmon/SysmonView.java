package com.guinetik.hexafun.examples.sysmon;

import static com.guinetik.hexafun.examples.sysmon.SysmonKeys.*;
import static com.guinetik.hexafun.examples.tui.Ansi.*;

import com.guinetik.hexafun.examples.tui.View;

/**
 * Responsive view components for the System Monitor TUI.
 *
 * <p>All views use {@code state.width()} to render at the current terminal width.
 * Width is recalculated on each refresh.</p>
 */
public final class SysmonView {

    private SysmonView() {}

    private static final int PADDING = 4; // Left/right padding

    /**
     * Compose the full screen view.
     */
    public static View<SysmonState> screen() {
        return clear()
            .andThen(header())
            .andThen(metrics())
            .andThen(menu())
            .andThen(status())
            .andThen(prompt());
    }

    /**
     * Clear screen and reset cursor.
     */
    public static View<SysmonState> clear() {
        return state -> CLEAR + CURSOR_HOME;
    }

    /**
     * Render the header box - responsive to terminal width.
     */
    public static View<SysmonState> header() {
        return state -> {
            int boxWidth = state.width() - PADDING;
            int innerWidth = boxWidth - 2;

            return lines(
                "",
                color("  " + DBOX_TOP_LEFT + repeat(DBOX_HORIZONTAL, innerWidth) + DBOX_TOP_RIGHT, CYAN),
                color("  " + DBOX_VERTICAL, CYAN) +
                    color(center("SYSTEM MONITOR", innerWidth), BOLD, BRIGHT_WHITE) +
                    color(DBOX_VERTICAL, CYAN),
                color("  " + DBOX_VERTICAL, CYAN) +
                    color(center("HexaFun Adapter Demo", innerWidth), DIM) +
                    color(DBOX_VERTICAL, CYAN),
                color("  " + DBOX_BOTTOM_LEFT + repeat(DBOX_HORIZONTAL, innerWidth) + DBOX_BOTTOM_RIGHT, CYAN),
                ""
            );
        };
    }

    /**
     * Render the metrics output based on selected format - responsive.
     */
    public static View<SysmonState> metrics() {
        return state -> {
            SystemMetrics m = state.metrics();
            int boxWidth = state.width() - PADDING;

            String output = switch (state.format()) {
                case TUI -> renderTuiGauges(m, boxWidth);
                case CLI -> formatBox("CLI Output", state.app().adapt(TO_CLI, m), boxWidth, GREEN);
                case JSON -> formatBox("JSON Output", state.app().adapt(TO_JSON, m), boxWidth, YELLOW);
                case PROMETHEUS -> formatBox("Prometheus Output", state.app().adapt(TO_PROMETHEUS, m), boxWidth, MAGENTA);
            };
            return output + "\n";
        };
    }

    /**
     * Render TUI gauges - responsive bar width.
     */
    private static String renderTuiGauges(SystemMetrics metrics, int boxWidth) {
        StringBuilder sb = new StringBuilder();
        int innerWidth = boxWidth - 2;
        // Gauge content: space(1) + label(4) + space(1) + bracket(1) + bar + bracket(1) + percent(5) + warn(2) + space(1) = 16 fixed
        int barWidth = innerWidth - 16;

        String title = "─ System Monitor ";  // 17 chars

        // Header
        sb.append(color("  " + BOX_TOP_LEFT + title +
            repeat(BOX_HORIZONTAL, innerWidth - title.length()) + BOX_TOP_RIGHT, CYAN)).append("\n");

        // CPU gauge
        sb.append(color("  " + BOX_VERTICAL, CYAN))
          .append(gauge("CPU ", metrics.cpu(), metrics.cpuWarning(), barWidth))
          .append(color(BOX_VERTICAL, CYAN)).append("\n");

        // Memory gauge
        sb.append(color("  " + BOX_VERTICAL, CYAN))
          .append(gauge("MEM ", metrics.memory(), metrics.memoryWarning(), barWidth))
          .append(color(BOX_VERTICAL, CYAN)).append("\n");

        // Disk gauge
        sb.append(color("  " + BOX_VERTICAL, CYAN))
          .append(gauge("DISK", metrics.disk(), metrics.diskWarning(), barWidth))
          .append(color(BOX_VERTICAL, CYAN)).append("\n");

        // Footer
        sb.append(color("  " + BOX_BOTTOM_LEFT + repeat(BOX_HORIZONTAL, innerWidth) +
            BOX_BOTTOM_RIGHT, CYAN)).append("\n");

        return sb.toString();
    }

    /**
     * Build a single gauge line with color-coded bar.
     */
    private static String gauge(String label, double percent, boolean warning, int barWidth) {
        int filled = (int) ((barWidth * percent) / 100);
        int empty = barWidth - filled;

        String barColor = warning ? RED : GREEN;
        String warnIcon = warning ? color(" ⚠", YELLOW) : "  ";

        return " " + color(label, BOLD) + " " +
            color("[", DIM) +
            color(repeat(BLOCK_FULL, filled), barColor) +
            color(repeat(BLOCK_LIGHT, empty), BRIGHT_BLACK) +
            color("]", DIM) +
            color(String.format(" %3.0f%%", percent), warning ? RED : WHITE) +
            warnIcon + " ";
    }

    /**
     * Format content in a responsive box with title.
     */
    private static String formatBox(String title, String content, int boxWidth, String borderColor) {
        StringBuilder sb = new StringBuilder();
        int innerWidth = boxWidth - 2;
        String titlePrefix = "─ " + title + " ";  // Calculate actual length

        // Top border with title
        sb.append(color("  " + BOX_TOP_LEFT + titlePrefix +
            repeat(BOX_HORIZONTAL, innerWidth - titlePrefix.length()) + BOX_TOP_RIGHT, borderColor))
          .append("\n");

        // Content lines: border(1) + space(1) + content + space(1) + border(1) = boxWidth
        // So content width = boxWidth - 4 = innerWidth - 2
        int contentWidth = innerWidth - 2;
        for (String line : content.split("\n")) {
            if (!line.isEmpty()) {
                String truncated = line.length() > contentWidth
                    ? line.substring(0, Math.max(0, contentWidth - 3)) + "..."
                    : line;
                sb.append(color("  " + BOX_VERTICAL, borderColor))
                  .append(" ")
                  .append(pad(truncated, contentWidth))
                  .append(" ")
                  .append(color(BOX_VERTICAL, borderColor))
                  .append("\n");
            }
        }

        // Bottom border
        sb.append(color("  " + BOX_BOTTOM_LEFT + repeat(BOX_HORIZONTAL, innerWidth) +
            BOX_BOTTOM_RIGHT, borderColor))
          .append("\n");

        return sb.toString();
    }

    /**
     * Render the format selection menu.
     */
    public static View<SysmonState> menu() {
        return state -> {
            int boxWidth = state.width() - PADDING;
            StringBuilder sb = new StringBuilder();

            sb.append(color("  " + repeat(BOX_HORIZONTAL, boxWidth), DIM))
              .append("\n\n");

            // Format options
            sb.append("  ");
            int i = 1;
            for (SysmonFormat f : SysmonFormat.values()) {
                String sel = f == state.format() ? "●" : "○";
                sb.append(color("[" + i + "]", f.color, BOLD));
                sb.append(
                    color(
                        " " + sel + " " + f.label + "  ",
                        f == state.format() ? f.color : BRIGHT_BLACK
                    )
                );
                i++;
            }
            sb.append("\n\n");

            // Other options
            sb.append("  ");
            sb.append(color("[r]", YELLOW, BOLD))
              .append(color(" Refresh  ", YELLOW));
            sb.append(color("[q]", BRIGHT_BLACK, BOLD))
              .append(color(" Quit", BRIGHT_BLACK));
            sb.append("\n\n");

            return sb.toString();
        };
    }

    /**
     * Render status message.
     */
    public static View<SysmonState> status() {
        return state ->
            state.status().isEmpty()
                ? "\n"
                : color(
                      "  " + ARROW_RIGHT + " " + state.status(),
                      state.statusColor()
                  ) +
                  "\n\n";
    }

    /**
     * Render input prompt.
     */
    public static View<SysmonState> prompt() {
        return state -> color("  > ", CYAN, BOLD);
    }
}
