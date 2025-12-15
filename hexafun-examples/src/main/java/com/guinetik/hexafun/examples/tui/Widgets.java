package com.guinetik.hexafun.examples.tui;

import static com.guinetik.hexafun.examples.tui.Ansi.*;

import java.util.List;
import java.util.function.Function;

/**
 * Reusable TUI widget builders.
 *
 * <p>All methods return Strings (pure functions), keeping side effects
 * at the edges. Compose these with {@link View} for full screens.
 *
 * <p>Example:
 * <pre class="language-java">{@code
 * String header = Widgets.header("My App", 80, CYAN);
 * String progress = Widgets.progressBar(75, 60, GREEN);
 * String box = Widgets.box("Content here", 40, YELLOW);
 * }</pre>
 */
public final class Widgets {

    private Widgets() {}

    // ═══════════════════════════════════════════════════════════════════
    //  HEADERS & TITLES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Render a boxed header with title and optional subtitle.
     *
     * @param title Main title text
     * @param subtitle Optional subtitle (can be null)
     * @param width Total width including borders
     * @param borderColor ANSI color for the border
     * @return Rendered header string
     */
    public static String header(
        String title,
        String subtitle,
        int width,
        String borderColor
    ) {
        int innerWidth = width - 4;
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb
            .append(
                color(
                    "  " +
                        DBOX_TOP_LEFT +
                        repeat(DBOX_HORIZONTAL, innerWidth) +
                        DBOX_TOP_RIGHT,
                    borderColor
                )
            )
            .append("\n");
        sb
            .append(color("  " + DBOX_VERTICAL, borderColor))
            .append(color(center(title, innerWidth), BOLD, BRIGHT_WHITE))
            .append(color(DBOX_VERTICAL, borderColor))
            .append("\n");

        if (subtitle != null && !subtitle.isEmpty()) {
            sb
                .append(color("  " + DBOX_VERTICAL, borderColor))
                .append(color(center(subtitle, innerWidth), DIM))
                .append(color(DBOX_VERTICAL, borderColor))
                .append("\n");
        }

        sb
            .append(
                color(
                    "  " +
                        DBOX_BOTTOM_LEFT +
                        repeat(DBOX_HORIZONTAL, innerWidth) +
                        DBOX_BOTTOM_RIGHT,
                    borderColor
                )
            )
            .append("\n");
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Simple header without subtitle.
     */
    public static String header(String title, int width, String borderColor) {
        return header(title, null, width, borderColor);
    }

    /**
     * Section divider with label.
     *
     * @param label Section label
     * @param width Total width
     * @return Rendered divider
     */
    public static String section(String label, int width) {
        int labelWidth = label.length() + 2;
        int lineWidth = width - 4 - labelWidth;
        return (
            color(
                "  " +
                    BOX_HORIZONTAL +
                    " " +
                    label +
                    " " +
                    repeat(BOX_HORIZONTAL, lineWidth),
                DIM
            ) +
            "\n\n"
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PROGRESS & STATS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Render a progress bar.
     *
     * @param percent Completion percentage (0-100)
     * @param width Bar width in characters
     * @param fillColor Color for filled portion
     * @return Rendered progress bar
     */
    public static String progressBar(int percent, int width, String fillColor) {
        int clamped = Math.max(0, Math.min(100, percent));
        int filled = (width * clamped) / 100;
        int empty = width - filled;

        return (
            "  " +
            color(repeat(BLOCK_FULL, filled), fillColor) +
            color(repeat(BLOCK_LIGHT, empty), BRIGHT_BLACK) +
            color(String.format(" %3d%%", clamped), DIM) +
            "\n"
        );
    }

    /**
     * Powerline-style segment for stats bars.
     *
     * @param text Segment text
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @return Rendered segment (without separator)
     */
    public static String segment(
        String text,
        int width,
        String bgColor,
        String fgColor
    ) {
        return color(center(text, width), bgColor, fgColor, BOLD);
    }

    /**
     * Powerline separator arrow.
     *
     * @param fromColor Color of previous segment
     * @param toColor Color of next segment (or null for end)
     * @return Rendered separator
     */
    public static String separator(String fromColor, String toColor) {
        if (toColor == null) {
            return color(PL_LEFT, fromColor);
        }
        return color(
            PL_LEFT,
            fromColor,
            toColor.replace("\u001B[3", "\u001B[4")
        ); // Convert fg to bg
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BOXES & CONTAINERS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Render content in a single-line box.
     *
     * @param content Content to box
     * @param width Box width
     * @param borderColor Border color
     * @return Boxed content
     */
    public static String box(String content, int width, String borderColor) {
        int innerWidth = width - 2;
        StringBuilder sb = new StringBuilder();

        sb
            .append(
                color(
                    BOX_TOP_LEFT +
                        repeat(BOX_HORIZONTAL, innerWidth) +
                        BOX_TOP_RIGHT,
                    borderColor
                )
            )
            .append("\n");
        sb
            .append(color(BOX_VERTICAL, borderColor))
            .append(center(content, innerWidth))
            .append(color(BOX_VERTICAL, borderColor))
            .append("\n");
        sb
            .append(
                color(
                    BOX_BOTTOM_LEFT +
                        repeat(BOX_HORIZONTAL, innerWidth) +
                        BOX_BOTTOM_RIGHT,
                    borderColor
                )
            )
            .append("\n");

        return sb.toString();
    }

    /**
     * Render multiple lines in a box.
     *
     * @param lines Content lines
     * @param width Box width
     * @param borderColor Border color
     * @return Boxed content
     */
    public static String box(
        List<String> lines,
        int width,
        String borderColor
    ) {
        int innerWidth = width - 2;
        StringBuilder sb = new StringBuilder();

        sb
            .append(
                color(
                    BOX_TOP_LEFT +
                        repeat(BOX_HORIZONTAL, innerWidth) +
                        BOX_TOP_RIGHT,
                    borderColor
                )
            )
            .append("\n");
        for (String line : lines) {
            String padded = line.length() > innerWidth
                ? line.substring(0, innerWidth)
                : pad(line, innerWidth);
            sb
                .append(color(BOX_VERTICAL, borderColor))
                .append(padded)
                .append(color(BOX_VERTICAL, borderColor))
                .append("\n");
        }
        sb
            .append(
                color(
                    BOX_BOTTOM_LEFT +
                        repeat(BOX_HORIZONTAL, innerWidth) +
                        BOX_BOTTOM_RIGHT,
                    borderColor
                )
            )
            .append("\n");

        return sb.toString();
    }

    /**
     * Horizontal rule/divider.
     *
     * @param width Total width
     * @return Rendered divider
     */
    public static String hr(int width) {
        return color("  " + repeat(BOX_HORIZONTAL, width - 4), DIM) + "\n";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LISTS & ITEMS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Render a numbered list item.
     *
     * @param index Item number
     * @param icon Icon to display
     * @param iconColor Icon color
     * @param text Item text
     * @param textColor Text color
     * @return Rendered list item
     */
    public static String listItem(
        int index,
        String icon,
        String iconColor,
        String text,
        String textColor
    ) {
        return (
            "    " +
            color(index + " ", BRIGHT_BLACK) +
            color(icon + " ", iconColor) +
            color(text, textColor) +
            "\n"
        );
    }

    /**
     * Render a simple bullet item.
     */
    public static String bulletItem(String text) {
        return "    " + color(BULLET + " ", DIM) + text + "\n";
    }

    /**
     * Render a numbered selection item (for menus).
     */
    public static String selectionItem(int index, String text) {
        return color("    [" + index + "] ", BRIGHT_BLACK) + text + "\n";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  MENUS & PROMPTS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Menu item with key shortcut.
     *
     * @param key Shortcut key
     * @param icon Icon
     * @param label Menu label
     * @param keyColor Color for the key
     * @return Rendered menu item
     */
    public static String menuItem(
        String key,
        String icon,
        String label,
        String keyColor
    ) {
        return (
            color("[" + key + "]", keyColor, BOLD) +
            color(" " + icon + " " + label + "  ", keyColor)
        );
    }

    /**
     * Input prompt.
     *
     * @param symbol Prompt symbol
     * @param promptColor Prompt color
     * @return Rendered prompt
     */
    public static String prompt(String symbol, String promptColor) {
        return color("  " + symbol + " ", promptColor, BOLD);
    }

    /**
     * Default prompt with ">".
     */
    public static String prompt(String promptColor) {
        return prompt(">", promptColor);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  STATUS & MESSAGES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Status message with arrow indicator.
     *
     * @param message Status message
     * @param messageColor Message color
     * @return Rendered status
     */
    public static String status(String message, String messageColor) {
        if (message == null || message.isEmpty()) return "\n";
        return color("  " + ARROW_RIGHT + " " + message, messageColor) + "\n\n";
    }

    /**
     * Success message.
     */
    public static String success(String message) {
        return status(CHECK + " " + message, GREEN);
    }

    /**
     * Error message.
     */
    public static String error(String message) {
        return status(CROSS + " " + message, RED);
    }

    /**
     * Warning message.
     */
    public static String warning(String message) {
        return status(BULLET + " " + message, YELLOW);
    }

    /**
     * Info message.
     */
    public static String info(String message) {
        return status(ICON_INFO + " " + message, BLUE);
    }
}
