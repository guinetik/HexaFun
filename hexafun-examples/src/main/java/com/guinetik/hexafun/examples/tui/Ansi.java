package com.guinetik.hexafun.examples.tui;

/**
 * ANSI escape code utilities for terminal styling.
 * No external dependencies - pure Java terminal control.
 *
 * <p>Usage:
 * <pre class="language-java">{@code
 * import static com.guinetik.hexafun.examples.tui.Ansi.*;
 *
 * System.out.println(color("Hello", GREEN, BOLD));
 * System.out.println(color(center("Title", 40), BG_BLUE, WHITE));
 * }</pre>
 */
public final class Ansi {

    private Ansi() {}

    // ═══════════════════════════════════════════════════════════════════
    //  TEXT STYLES
    // ═══════════════════════════════════════════════════════════════════

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";

    // ═══════════════════════════════════════════════════════════════════
    //  FOREGROUND COLORS
    // ═══════════════════════════════════════════════════════════════════

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bright variants
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // ═══════════════════════════════════════════════════════════════════
    //  BACKGROUND COLORS
    // ═══════════════════════════════════════════════════════════════════

    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";

    // ═══════════════════════════════════════════════════════════════════
    //  CURSOR & SCREEN CONTROL
    // ═══════════════════════════════════════════════════════════════════

    public static final String CLEAR_SCREEN = "\u001B[2J";
    public static final String CLEAR = CLEAR_SCREEN;
    public static final String CURSOR_HOME = "\u001B[H";
    public static final String HIDE_CURSOR = "\u001B[?25l";
    public static final String SHOW_CURSOR = "\u001B[?25h";

    // Alternate screen buffer (doesn't pollute scrollback)
    public static final String ALT_SCREEN_ON = "\u001B[?1049h";
    public static final String ALT_SCREEN_OFF = "\u001B[?1049l";

    // ═══════════════════════════════════════════════════════════════════
    //  BOX DRAWING - UNICODE
    // ═══════════════════════════════════════════════════════════════════

    // Single line
    public static final String BOX_TOP_LEFT = "\u250C"; // ┌
    public static final String BOX_TOP_RIGHT = "\u2510"; // ┐
    public static final String BOX_BOTTOM_LEFT = "\u2514"; // └
    public static final String BOX_BOTTOM_RIGHT = "\u2518"; // ┘
    public static final String BOX_HORIZONTAL = "\u2500"; // ─
    public static final String BOX_VERTICAL = "\u2502"; // │
    public static final String BOX_T_DOWN = "\u252C"; // ┬
    public static final String BOX_T_UP = "\u2534"; // ┴
    public static final String BOX_T_RIGHT = "\u251C"; // ├
    public static final String BOX_T_LEFT = "\u2524"; // ┤
    public static final String BOX_CROSS = "\u253C"; // ┼

    // Double line
    public static final String DBOX_TOP_LEFT = "\u2554"; // ╔
    public static final String DBOX_TOP_RIGHT = "\u2557"; // ╗
    public static final String DBOX_BOTTOM_LEFT = "\u255A"; // ╚
    public static final String DBOX_BOTTOM_RIGHT = "\u255D"; // ╝
    public static final String DBOX_HORIZONTAL = "\u2550"; // ═
    public static final String DBOX_VERTICAL = "\u2551"; // ║

    // ═══════════════════════════════════════════════════════════════════
    //  NERDFONT SYMBOLS
    // ═══════════════════════════════════════════════════════════════════

    // Common symbols
    public static final String CHECK = "\uEAB2"; // nf-cod-check
    public static final String CROSS = "\uEA76"; // nf-cod-close
    public static final String BULLET = "\uEABC"; // nf-cod-circle
    public static final String ARROW_RIGHT = "\uEA9C"; // nf-cod-arrow_right
    public static final String ARROW_LEFT = "\uEA9B"; // nf-cod-arrow_left
    public static final String STAR = "\uF005"; // nf-fa-star
    public static final String EMPTY_STAR = "\uEA6A"; // nf-cod-star_empty

    // Icons
    public static final String ICON_TASK = "\uEB67"; // nf-cod-tasklist
    public static final String ICON_DASHBOARD = "\uEACD"; // nf-cod-dashboard
    public static final String ICON_ADD = "\uEA60"; // nf-cod-add
    public static final String ICON_TRASH = "\uEA81"; // nf-cod-trash
    public static final String ICON_CHECK_ALL = "\uEBB1"; // nf-cod-check_all
    public static final String ICON_INBOX = "\uEB09"; // nf-cod-inbox
    public static final String ICON_FOLDER = "\uEA83"; // nf-cod-folder
    public static final String ICON_FLAME = "\uEAF2"; // nf-cod-flame
    public static final String ICON_GEAR = "\uEAF8"; // nf-cod-gear
    public static final String ICON_EYE = "\uEAE5"; // nf-cod-eye
    public static final String ICON_INFO = "\uEAFC"; // nf-cod-info
    public static final String ICON_EDIT = "\uEA73"; // nf-cod-edit
    public static final String ICON_SEARCH = "\uEA6D"; // nf-cod-search
    public static final String ICON_HOME = "\uEAF0"; // nf-cod-home
    public static final String ICON_TERMINAL = "\uEA85"; // nf-cod-terminal

    // Progress bar blocks
    public static final String BLOCK_FULL = "\u2588"; // █
    public static final String BLOCK_LIGHT = "\u2591"; // ░
    public static final String BLOCK_MED = "\u2592"; // ▒
    public static final String BLOCK_DARK = "\u2593"; // ▓

    // Powerline separators
    public static final String PL_LEFT = "\uE0B0"; // nf-pl-left_hard_divider
    public static final String PL_RIGHT = "\uE0B2"; // nf-pl-right_hard_divider
    public static final String PL_LEFT_SOFT = "\uE0B1"; // nf-pl-left_soft_divider
    public static final String PL_RIGHT_SOFT = "\uE0B3"; // nf-pl-right_soft_divider

    // ═══════════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════

    /** Apply ANSI codes to text with auto-reset */
    public static String color(String text, String... codes) {
        if (codes.length == 0) return text;
        StringBuilder sb = new StringBuilder();
        for (String code : codes) {
            sb.append(code);
        }
        return sb.append(text).append(RESET).toString();
    }

    /** Bold text shorthand */
    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    /** Dim text shorthand */
    public static String dim(String text) {
        return DIM + text + RESET;
    }

    /** Clear screen (side effect) */
    public static void clear() {
        System.out.print(CLEAR_SCREEN + CURSOR_HOME);
        System.out.flush();
    }

    /** Hide cursor (side effect) */
    public static void hideCursor() {
        System.out.print(HIDE_CURSOR);
        System.out.flush();
    }

    /** Show cursor (side effect) */
    public static void showCursor() {
        System.out.print(SHOW_CURSOR);
        System.out.flush();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  STRING UTILITIES
    // ═══════════════════════════════════════════════════════════════════

    /** Repeat a character n times */
    public static String repeat(char c, int count) {
        return String.valueOf(c).repeat(Math.max(0, count));
    }

    /** Repeat a string n times */
    public static String repeat(String s, int count) {
        return s.repeat(Math.max(0, count));
    }

    /** Right-pad string to width */
    public static String pad(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }

    /** Left-pad string to width */
    public static String padLeft(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return " ".repeat(width - text.length()) + text;
    }

    /** Center string within width */
    public static String center(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        return (
            " ".repeat(padding) +
            text +
            " ".repeat(width - text.length() - padding)
        );
    }

    /** Truncate string with ellipsis if too long */
    public static String truncate(String text, int maxWidth) {
        if (text.length() <= maxWidth) return text;
        return text.substring(0, Math.max(0, maxWidth - 3)) + "...";
    }

    /** Join strings with newlines */
    public static String lines(String... lines) {
        return String.join("\n", lines) + "\n";
    }
}
