package com.guinetik.hexafun.examples;

import com.guinetik.hexafun.examples.counter.CounterApp;
import com.guinetik.hexafun.examples.sysmon.SysmonTUI;
import com.guinetik.hexafun.examples.tasks.tui.TasksTUI;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.guinetik.hexafun.examples.tui.Ansi.*;

/**
 * Interactive launcher for HexaFun examples.
 *
 * <p>Run with: {@code mvn exec:java -pl hexafun-examples}</p>
 */
public class ExamplesLauncher {

    private static final String LOGO = """

              ╦ ╦┌─┐─┐ ┬┌─┐╔═╗┬ ┬┌┐┌
              ╠═╣├┤ ┌┴┬┘├─┤╠╣ │ ││││
              ╩ ╩└─┘┴ └─┴ ┴╚  └─┘┘└┘
            """;

    private static final String MENU = """

              %s Examples %s

              %s1%s  Counter    Simple increment/add with validation
              %s2%s  Tasks      Kanban board TUI (TODO → DOING → DONE)
              %s3%s  Sysmon     System monitor with multiple output formats

              %sq%s  Quit

            """.formatted(
            color("─────", DIM), color("─────", DIM),
            color("[", DIM) + color("1", CYAN) + color("]", DIM), "",
            color("[", DIM) + color("2", CYAN) + color("]", DIM), "",
            color("[", DIM) + color("3", CYAN) + color("]", DIM), "",
            color("[", DIM) + color("q", YELLOW) + color("]", DIM), ""
    );

    public static void main(String[] args) {
        // If an argument is passed, launch that example directly
        if (args.length > 0) {
            launchByName(args[0]);
            return;
        }

        // Interactive menu
        var reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(CLEAR_SCREEN + CURSOR_HOME);
            System.out.println(color(LOGO, CYAN));
            System.out.println(MENU);
            System.out.print(color("  Select example: ", BOLD));

            try {
                String input = reader.readLine();
                if (input == null || input.equalsIgnoreCase("q")) {
                    System.out.println(color("\n  Goodbye!\n", DIM));
                    break;
                }

                switch (input.trim()) {
                    case "1", "counter" -> {
                        System.out.println(color("\n  Launching Counter...\n", GREEN));
                        CounterApp.main(new String[]{});
                        pause(reader);
                    }
                    case "2", "tasks" -> {
                        System.out.println(color("\n  Launching Tasks TUI...\n", GREEN));
                        TasksTUI.main(new String[]{});
                    }
                    case "3", "sysmon" -> {
                        System.out.println(color("\n  Launching System Monitor...\n", GREEN));
                        new SysmonTUI().run();
                    }
                    default -> {
                        System.out.println(color("\n  Unknown option: " + input, RED));
                        pause(reader);
                    }
                }
            } catch (Exception e) {
                System.out.println(color("\n  Error: " + e.getMessage(), RED));
            }
        }
    }

    private static void launchByName(String name) {
        switch (name.toLowerCase()) {
            case "counter", "1" -> CounterApp.main(new String[]{});
            case "tasks", "2" -> TasksTUI.main(new String[]{});
            case "sysmon", "3" -> new SysmonTUI().run();
            default -> {
                System.out.println("Unknown example: " + name);
                System.out.println("Available: counter, tasks, sysmon");
            }
        }
    }

    private static void pause(BufferedReader reader) {
        System.out.print(color("\n  Press Enter to continue...", DIM));
        try {
            reader.readLine();
        } catch (Exception ignored) {}
    }
}
