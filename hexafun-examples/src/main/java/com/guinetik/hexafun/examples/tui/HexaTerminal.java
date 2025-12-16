package com.guinetik.hexafun.examples.tui;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Cross-platform terminal utility functions for TUI applications.
 *
 * <p>Provides terminal dimension detection on Unix, macOS, and Windows.</p>
 */
public final class HexaTerminal {

    private HexaTerminal() {}

    /** Default minimum width for TUI applications */
    public static final int MIN_WIDTH = 60;

    /** Default width if detection fails */
    public static final int DEFAULT_WIDTH = 120;

    /** Default minimum height */
    public static final int MIN_HEIGHT = 20;

    /** Default height if detection fails */
    public static final int DEFAULT_HEIGHT = 40;

    /** Cached OS type */
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "")
        .toLowerCase().contains("win");

    /**
     * Detect the current terminal width.
     *
     * @return Terminal width in characters
     */
    public static int detectWidth() {
        return detectWidth(MIN_WIDTH, DEFAULT_WIDTH);
    }

    /**
     * Detect terminal width with custom bounds.
     *
     * @param minWidth Minimum width to return
     * @param defaultWidth Default if detection fails
     * @return Terminal width in characters
     */
    public static int detectWidth(int minWidth, int defaultWidth) {
        int[] size = detectSize();
        if (size != null && size[1] > 0) {
            return Math.max(minWidth, size[1]);
        }
        return defaultWidth;
    }

    /**
     * Detect the current terminal height.
     *
     * @return Terminal height in rows
     */
    public static int detectHeight() {
        return detectHeight(MIN_HEIGHT, DEFAULT_HEIGHT);
    }

    /**
     * Detect terminal height with custom bounds.
     *
     * @param minHeight Minimum height to return
     * @param defaultHeight Default if detection fails
     * @return Terminal height in rows
     */
    public static int detectHeight(int minHeight, int defaultHeight) {
        int[] size = detectSize();
        if (size != null && size[0] > 0) {
            return Math.max(minHeight, size[0]);
        }
        return defaultHeight;
    }

    /**
     * Detect terminal size as [rows, columns].
     *
     * @return int[2] with {rows, columns} or null if detection fails
     */
    public static int[] detectSize() {
        return IS_WINDOWS ? detectSizeWindows() : detectSizeUnix();
    }

    /**
     * Unix/macOS/WSL size detection using stty.
     */
    private static int[] detectSizeUnix() {
        // Try stty size (works on Linux, macOS, WSL)
        try {
            ProcessBuilder pb = new ProcessBuilder("stty", "size");
            pb.inheritIO();
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);

            Process p = pb.start();
            String line = new BufferedReader(
                new InputStreamReader(p.getInputStream())
            ).readLine();

            int exitCode = p.waitFor();
            if (exitCode == 0 && line != null && !line.isBlank()) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    int rows = Integer.parseInt(parts[0]);
                    int cols = Integer.parseInt(parts[1]);
                    if (rows > 0 && cols > 0) {
                        return new int[]{rows, cols};
                    }
                }
            }
        } catch (Exception ignored) {}

        // Fallback: tput
        try {
            int cols = runCommandInt("tput", "cols");
            int rows = runCommandInt("tput", "lines");
            if (cols > 0 && rows > 0) {
                return new int[]{rows, cols};
            }
        } catch (Exception ignored) {}

        // Fallback: environment variables
        return detectSizeFromEnv();
    }

    /**
     * Windows size detection using PowerShell or MODE.
     */
    private static int[] detectSizeWindows() {
        // Try PowerShell (most reliable on modern Windows)
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-NoProfile", "-Command",
                "[Console]::WindowWidth; [Console]::WindowHeight"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream())
            );
            String widthLine = reader.readLine();
            String heightLine = reader.readLine();
            p.waitFor();

            if (widthLine != null && heightLine != null) {
                int cols = Integer.parseInt(widthLine.trim());
                int rows = Integer.parseInt(heightLine.trim());
                if (rows > 0 && cols > 0) {
                    return new int[]{rows, cols};
                }
            }
        } catch (Exception ignored) {}

        // Fallback: MODE CON (cmd.exe)
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "mode", "con");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream())
            );

            int cols = -1, rows = -1;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toLowerCase();
                if (line.contains("columns") || line.contains("cols")) {
                    cols = extractNumber(line);
                } else if (line.contains("lines")) {
                    rows = extractNumber(line);
                }
            }
            p.waitFor();

            if (cols > 0 && rows > 0) {
                return new int[]{rows, cols};
            }
        } catch (Exception ignored) {}

        // Fallback: environment variables
        return detectSizeFromEnv();
    }

    /**
     * Try to get size from environment variables.
     */
    private static int[] detectSizeFromEnv() {
        String colsEnv = System.getenv("COLUMNS");
        String rowsEnv = System.getenv("LINES");
        if (colsEnv != null && rowsEnv != null) {
            try {
                int cols = Integer.parseInt(colsEnv.trim());
                int rows = Integer.parseInt(rowsEnv.trim());
                if (rows > 0 && cols > 0) {
                    return new int[]{rows, cols};
                }
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    /**
     * Run a command and return the output as an integer.
     */
    private static int runCommandInt(String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String line = new BufferedReader(
            new InputStreamReader(p.getInputStream())
        ).readLine();
        p.waitFor();
        return line != null ? Integer.parseInt(line.trim()) : -1;
    }

    /**
     * Extract a number from a string like "Columns: 120" or "Lines: 30".
     */
    private static int extractNumber(String line) {
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (sb.length() > 0) {
                break; // Stop at first non-digit after finding digits
            }
        }
        try {
            return sb.length() > 0 ? Integer.parseInt(sb.toString()) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Get terminal size as "ROWSxCOLS" string.
     *
     * @return Size string like "24x80" or "unknown"
     */
    public static String getSizeString() {
        int[] size = detectSize();
        if (size != null) {
            return size[0] + "x" + size[1];
        }
        return "unknown";
    }

    /**
     * Check if running on Windows.
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }
}
