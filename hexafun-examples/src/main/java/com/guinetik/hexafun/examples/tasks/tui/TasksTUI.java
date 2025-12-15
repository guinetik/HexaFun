package com.guinetik.hexafun.examples.tasks.tui;

import static com.guinetik.hexafun.examples.tui.Ansi.*;

import com.guinetik.hexafun.examples.tasks.Task;
import com.guinetik.hexafun.examples.tasks.TaskApp;
import com.guinetik.hexafun.examples.tasks.TaskStatus;
import com.guinetik.hexafun.examples.tui.View;
import com.guinetik.hexafun.examples.tui.Widgets;
import com.guinetik.hexafun.fun.Result;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Functional TUI for the Tasks application.
 *
 * <p>Demonstrates functional composition in a terminal UI using the
 * shared {@link com.guinetik.hexafun.examples.tui} package:
 * <ul>
 *   <li>{@link State} - Immutable state record</li>
 *   <li>{@link View} - Pure function S → String (from shared tui package)</li>
 *   <li>{@link Views} - Composable view components</li>
 *   <li>{@link Widgets} - Reusable TUI widgets (from shared tui package)</li>
 *   <li>Single {@link #render(State)} side effect</li>
 * </ul>
 *
 * <p>The pattern: {@code render(views.screen().apply(state))}
 */
public class TasksTUI {

    // ═══════════════════════════════════════════════════════════════════
    //  IMMUTABLE STATE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Immutable TUI state. All state transitions return new State instances.
     */
    public record State(
        TaskApp app,
        int width,
        String status,
        String statusColor,
        boolean running
    ) {
        private static final int MIN_WIDTH = 60;
        private static final int KANBAN_MIN_WIDTH = 80;

        /** Create initial state */
        public static State initial(TaskApp app) {
            return new State(app, detectWidth(), "", GREEN, true);
        }

        /** State transitions - all return new State */
        public State withStatus(String msg, String color) {
            return new State(app, width, msg, color, running);
        }

        public State withWidth(int w) {
            return new State(
                app,
                Math.max(MIN_WIDTH, w),
                status,
                statusColor,
                running
            );
        }

        public State refreshWidth() {
            return withWidth(detectWidth());
        }

        public State stop() {
            return new State(app, width, "Goodbye!", CYAN, false);
        }

        /** Derived state */
        public boolean isKanban() {
            return width >= KANBAN_MIN_WIDTH;
        }

        public List<Task> tasks() {
            return app.listTasks();
        }

        public List<Task> byStatus(TaskStatus s) {
            return tasks()
                .stream()
                .filter(t -> t.status() == s)
                .toList();
        }

        /** Terminal width detection */
        private static int detectWidth() {
            // System property override
            String prop = System.getProperty("columns");
            if (prop != null) {
                try {
                    return Math.max(MIN_WIDTH, Integer.parseInt(prop.trim()));
                } catch (NumberFormatException ignored) {}
            }

            // COLUMNS env
            String env = System.getenv("COLUMNS");
            if (env != null) {
                try {
                    return Math.max(MIN_WIDTH, Integer.parseInt(env.trim()));
                } catch (NumberFormatException ignored) {}
            }

            // tput cols
            try {
                Process p = new ProcessBuilder("tput", "cols").start();
                String line = new BufferedReader(
                    new InputStreamReader(p.getInputStream())
                ).readLine();
                p.waitFor();
                if (line != null && !line.isBlank()) {
                    int w = Integer.parseInt(line.trim());
                    if (w > 0) return Math.max(MIN_WIDTH, w);
                }
            } catch (Exception ignored) {}

            return 120; // Default for Kanban
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  VIEWS - PURE VIEW COMPONENTS (using shared View<S> interface)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Pure view functions using the shared {@link View} interface.
     * Each transforms State → String with no side effects.
     */
    public static class Views {

        /** The complete screen - composed from all views */
        public static View<State> screen() {
            return clear()
                .andThen(header())
                .andThen(stats())
                .andThen(View.when(State::isKanban, kanban(), tasks()))
                .andThen(menu())
                .andThen(status())
                .andThen(prompt());
        }

        // ─────────────────────────────────────────────────────────────
        //  Individual view components
        // ─────────────────────────────────────────────────────────────

        public static View<State> clear() {
            return state -> CLEAR + CURSOR_HOME;
        }

        public static View<State> header() {
            return state -> {
                int w = state.width();
                int headerWidth = w - 4;
                String title = ICON_TASK + "  TASK MANAGER";
                String mode = state.isKanban() ? "KANBAN" : "LIST";
                String hint = state.isKanban()
                    ? ""
                    : " (<" + State.KANBAN_MIN_WIDTH + " cols)";
                String subtitle =
                    "HexaFun Demo  [" + w + "w " + mode + hint + "]";

                return lines(
                    "",
                    color(
                        "  " +
                            DBOX_TOP_LEFT +
                            repeat(DBOX_HORIZONTAL, headerWidth) +
                            DBOX_TOP_RIGHT,
                        CYAN
                    ),
                    color("  " + DBOX_VERTICAL, CYAN) +
                        color(center(title, headerWidth), BOLD, BRIGHT_WHITE) +
                        color(DBOX_VERTICAL, CYAN),
                    color("  " + DBOX_VERTICAL, CYAN) +
                        color(center(subtitle, headerWidth), DIM) +
                        color(DBOX_VERTICAL, CYAN),
                    color(
                        "  " +
                            DBOX_BOTTOM_LEFT +
                            repeat(DBOX_HORIZONTAL, headerWidth) +
                            DBOX_BOTTOM_RIGHT,
                        CYAN
                    ),
                    ""
                );
            };
        }

        public static View<State> stats() {
            return state -> {
                List<Task> tasks = state.tasks();
                long total = tasks.size();
                long todo = state.byStatus(TaskStatus.TODO).size();
                long doing = state.byStatus(TaskStatus.DOING).size();
                long done = state.byStatus(TaskStatus.DONE).size();
                int percent = total > 0 ? (int) ((done * 100) / total) : 0;

                // Fluid segment widths
                int available = state.width() - 6;
                int segW = available / 3;
                int extra = available % 3;

                String todoSeg = color(
                    center(BULLET + " " + todo + " TODO", segW + extra),
                    BG_YELLOW,
                    BLACK,
                    BOLD
                );
                String doingSeg = color(
                    center(ICON_FLAME + " " + doing + " DOING", segW),
                    BG_MAGENTA,
                    BRIGHT_WHITE,
                    BOLD
                );
                String doneSeg = color(
                    center(CHECK + " " + done + " DONE", segW),
                    BG_GREEN,
                    BLACK,
                    BOLD
                );

                // Progress bar
                int barW = state.width() - 8;
                int filled = (barW * percent) / 100;
                int empty = barW - filled;

                return lines(
                    "  " +
                        todoSeg +
                        color(PL_LEFT, YELLOW, BG_MAGENTA) +
                        doingSeg +
                        color(PL_LEFT, MAGENTA, BG_GREEN) +
                        doneSeg +
                        color(PL_LEFT, GREEN),
                    "",
                    "  " +
                        color(repeat(BLOCK_FULL, filled), GREEN) +
                        color(repeat(BLOCK_LIGHT, empty), BRIGHT_BLACK) +
                        color(String.format(" %3d%%", percent), DIM),
                    ""
                );
            };
        }

        public static View<State> tasks() {
            return state -> {
                int w = state.width();
                List<Task> tasks = state.tasks();
                int lineW = w - 4 - 9;

                StringBuilder sb = new StringBuilder();
                sb
                    .append(
                        color(
                            "  " +
                                BOX_HORIZONTAL +
                                " TASKS " +
                                repeat(BOX_HORIZONTAL, lineW),
                            DIM
                        )
                    )
                    .append("\n\n");

                if (tasks.isEmpty()) {
                    sb
                        .append(
                            color(
                                "  " +
                                    center(
                                        "No tasks yet. Press [a] to add one!",
                                        w - 4
                                    ),
                                DIM,
                                ITALIC
                            )
                        )
                        .append("\n\n");
                } else {
                    int idx = 1;
                    for (Task task : tasks) {
                        sb.append(taskCard(task, idx++, w));
                    }
                }
                return sb.toString();
            };
        }

        private static String taskCard(Task task, int index, int width) {
            String icon = switch (task.status()) {
                case TODO -> BULLET;
                case DOING -> ICON_FLAME;
                case DONE -> CHECK;
            };
            String iconColor = switch (task.status()) {
                case TODO -> YELLOW;
                case DOING -> MAGENTA;
                case DONE -> GREEN;
            };
            String titleColor = task.completed() ? DIM : BRIGHT_WHITE;
            String suffix = switch (task.status()) {
                case DONE -> color("  (done)", DIM, GREEN);
                case DOING -> color("  (wip)", DIM, MAGENTA);
                default -> "";
            };

            int suffixLen = task.status() == TaskStatus.DONE
                ? 8
                : (task.status() == TaskStatus.DOING ? 7 : 0);
            int available = width - 8 - suffixLen - 2;
            String title = task.title();
            if (title.length() > available) {
                title = title.substring(0, Math.max(0, available - 3)) + "...";
            }

            StringBuilder sb = new StringBuilder();
            sb
                .append("    ")
                .append(color(index + " ", BRIGHT_BLACK))
                .append(color(icon + " ", iconColor))
                .append(color(title, titleColor))
                .append(suffix)
                .append("\n");

            if (task.description() != null && !task.description().isBlank()) {
                int descW = width - 10;
                String desc = task.description();
                if (desc.length() > descW) desc =
                    desc.substring(0, Math.max(0, descW - 3)) + "...";
                sb.append(color("        " + desc, DIM)).append("\n");
            }
            sb.append("\n");
            return sb.toString();
        }

        public static View<State> kanban() {
            return state -> {
                List<Task> todo = state.byStatus(TaskStatus.TODO);
                List<Task> doing = state.byStatus(TaskStatus.DOING);
                List<Task> done = state.byStatus(TaskStatus.DONE);

                int totalW = state.width() - 4;
                int innerW = totalW - 2; // gutters
                int colW = innerW / 3;
                int extra = innerW % 3;
                int c1 = colW + extra,
                    c2 = colW,
                    c3 = colW;

                StringBuilder sb = new StringBuilder();

                // Top borders
                sb
                    .append("  ")
                    .append(
                        color(
                            BOX_TOP_LEFT +
                                repeat(BOX_HORIZONTAL, c1 - 2) +
                                BOX_TOP_RIGHT,
                            YELLOW
                        )
                    )
                    .append(" ")
                    .append(
                        color(
                            BOX_TOP_LEFT +
                                repeat(BOX_HORIZONTAL, c2 - 2) +
                                BOX_TOP_RIGHT,
                            MAGENTA
                        )
                    )
                    .append(" ")
                    .append(
                        color(
                            BOX_TOP_LEFT +
                                repeat(BOX_HORIZONTAL, c3 - 2) +
                                BOX_TOP_RIGHT,
                            GREEN
                        )
                    )
                    .append("\n");

                // Headers
                sb
                    .append("  ")
                    .append(color(BOX_VERTICAL, YELLOW))
                    .append(
                        color(
                            center(
                                BULLET + " TODO (" + todo.size() + ")",
                                c1 - 2
                            ),
                            BOLD,
                            YELLOW
                        )
                    )
                    .append(color(BOX_VERTICAL, YELLOW))
                    .append(" ")
                    .append(color(BOX_VERTICAL, MAGENTA))
                    .append(
                        color(
                            center(
                                ICON_FLAME + " DOING (" + doing.size() + ")",
                                c2 - 2
                            ),
                            BOLD,
                            MAGENTA
                        )
                    )
                    .append(color(BOX_VERTICAL, MAGENTA))
                    .append(" ")
                    .append(color(BOX_VERTICAL, GREEN))
                    .append(
                        color(
                            center(
                                CHECK + " DONE (" + done.size() + ")",
                                c3 - 2
                            ),
                            BOLD,
                            GREEN
                        )
                    )
                    .append(color(BOX_VERTICAL, GREEN))
                    .append("\n");

                // Separators
                sb
                    .append("  ")
                    .append(
                        color(
                            BOX_T_RIGHT +
                                repeat(BOX_HORIZONTAL, c1 - 2) +
                                BOX_T_LEFT,
                            YELLOW
                        )
                    )
                    .append(" ")
                    .append(
                        color(
                            BOX_T_RIGHT +
                                repeat(BOX_HORIZONTAL, c2 - 2) +
                                BOX_T_LEFT,
                            MAGENTA
                        )
                    )
                    .append(" ")
                    .append(
                        color(
                            BOX_T_RIGHT +
                                repeat(BOX_HORIZONTAL, c3 - 2) +
                                BOX_T_LEFT,
                            GREEN
                        )
                    )
                    .append("\n");

                // Task rows
                int maxRows = Math.max(
                    todo.size(),
                    Math.max(doing.size(), done.size())
                );
                if (maxRows == 0) maxRows = 1;

                int todoIdx = 1,
                    doingIdx = todo.size() + 1,
                    doneIdx = todo.size() + doing.size() + 1;

                for (int i = 0; i < maxRows; i++) {
                    sb.append("  ").append(color(BOX_VERTICAL, YELLOW));
                    sb.append(
                        i < todo.size()
                            ? color(
                                  kanbanCard(todo.get(i), todoIdx++, c1 - 2),
                                  BRIGHT_WHITE
                              )
                            : repeat(" ", c1 - 2)
                    );
                    sb.append(color(BOX_VERTICAL, YELLOW)).append(" ");

                    sb.append(color(BOX_VERTICAL, MAGENTA));
                    sb.append(
                        i < doing.size()
                            ? color(
                                  kanbanCard(doing.get(i), doingIdx++, c2 - 2),
                                  BRIGHT_MAGENTA
                              )
                            : repeat(" ", c2 - 2)
                    );
                    sb.append(color(BOX_VERTICAL, MAGENTA)).append(" ");

                    sb.append(color(BOX_VERTICAL, GREEN));
                    sb.append(
                        i < done.size()
                            ? color(
                                  kanbanCard(done.get(i), doneIdx++, c3 - 2),
                                  DIM
                              )
                            : repeat(" ", c3 - 2)
                    );
                    sb.append(color(BOX_VERTICAL, GREEN)).append("\n");
                }

                // Bottom borders
                sb
                    .append("  ")
                    .append(
                        color(
                            BOX_BOTTOM_LEFT +
                                repeat(BOX_HORIZONTAL, c1 - 2) +
                                BOX_BOTTOM_RIGHT,
                            YELLOW
                        )
                    )
                    .append(" ")
                    .append(
                        color(
                            BOX_BOTTOM_LEFT +
                                repeat(BOX_HORIZONTAL, c2 - 2) +
                                BOX_BOTTOM_RIGHT,
                            MAGENTA
                        )
                    )
                    .append(" ")
                    .append(
                        color(
                            BOX_BOTTOM_LEFT +
                                repeat(BOX_HORIZONTAL, c3 - 2) +
                                BOX_BOTTOM_RIGHT,
                            GREEN
                        )
                    )
                    .append("\n\n");

                return sb.toString();
            };
        }

        private static String kanbanCard(Task task, int index, int width) {
            String title = task.title();
            int maxLen = width - 4;
            if (title.length() > maxLen) title =
                title.substring(0, Math.max(0, maxLen - 2)) + "..";
            return pad(" " + index + " " + title, width);
        }

        public static View<State> menu() {
            return state ->
                lines(
                    color(
                        "  " + repeat(BOX_HORIZONTAL, state.width() - 4),
                        DIM
                    ),
                    "",
                    "  " +
                        color("[a]", CYAN, BOLD) +
                        color(" " + ICON_ADD + " Add  ", CYAN) +
                        color("[v]", BLUE, BOLD) +
                        color(" " + ICON_EYE + " View  ", BLUE) +
                        color("[s]", MAGENTA, BOLD) +
                        color(" " + ICON_FLAME + " Start  ", MAGENTA) +
                        color("[c]", GREEN, BOLD) +
                        color(" " + CHECK + " Done  ", GREEN) +
                        color("[d]", RED, BOLD) +
                        color(" " + ICON_TRASH + " Del  ", RED) +
                        color("[q]", BRIGHT_BLACK, BOLD) +
                        color(" " + CROSS + " Quit", BRIGHT_BLACK),
                    ""
                );
        }

        public static View<State> status() {
            return state ->
                state.status().isEmpty()
                    ? "\n"
                    : color(
                          "  " + ARROW_RIGHT + " " + state.status(),
                          state.statusColor()
                      ) +
                      "\n\n";
        }

        public static View<State> prompt() {
            return state -> color("  > ", CYAN, BOLD);
        }

    }

    // ═══════════════════════════════════════════════════════════════════
    //  ACTIONS - STATE TRANSITIONS VIA RESULT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Actions transform State → Result<State>.
     * Success = new state, Failure = error message to display.
     */
    @FunctionalInterface
    public interface Action extends Function<State, Result<State>> {
        /** Chain actions: if first succeeds, apply second */
        default Action andThen(Action next) {
            return state -> this.apply(state).flatMap(next);
        }

        /** Action that always succeeds with given state transform */
        static Action of(UnaryOperator<State> transform) {
            return state -> Result.ok(transform.apply(state));
        }

        /** Action from a Result-returning operation */
        static Action fromResult(Function<State, Result<State>> f) {
            return f::apply;
        }
    }

    /**
     * Action implementations for each command.
     */
    public static class Actions {

        public static Result<State> create(
            State state,
            String title,
            String desc
        ) {
            return state
                .app()
                .createTask(title, desc)
                .map(task ->
                    state.withStatus("Created: " + task.title(), GREEN)
                );
        }

        public static Result<State> start(State state, String taskId) {
            return state
                .app()
                .startTask(taskId)
                .map(task ->
                    state.withStatus("Started: " + task.title(), MAGENTA)
                );
        }

        public static Result<State> complete(State state, String taskId) {
            return state
                .app()
                .completeTask(taskId)
                .map(task ->
                    state.withStatus("Completed: " + task.title(), GREEN)
                );
        }

        public static Result<State> delete(State state, String taskId) {
            return state
                .app()
                .deleteTask(taskId)
                .map(ok -> state.withStatus("Deleted task", RED));
        }

        /** Quick action: advances task through workflow based on current status */
        public static Result<State> quickAction(State state, int index) {
            List<Task> tasks = state.tasks();
            if (index < 1 || index > tasks.size()) {
                return Result.fail("Invalid task number: " + index);
            }

            Task task = tasks.get(index - 1);
            return switch (task.status()) {
                case TODO -> start(state, task.id());
                case DOING -> complete(state, task.id());
                case DONE -> Result.ok(
                    state.withStatus(
                        "Already completed: " + task.title(),
                        YELLOW
                    )
                );
            };
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  RUNTIME - THE ONLY SIDE EFFECTS
    // ═══════════════════════════════════════════════════════════════════

    private final BufferedReader reader;
    private final View<State> screen;

    public TasksTUI() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.screen = Views.screen();
    }

    /** The ONE place where we print to console */
    private void render(State state) {
        System.out.print(screen.apply(state));
        System.out.flush();
    }

    /** Read a line of input */
    private String readLine() {
        try {
            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    /** Prompt for additional input during an action */
    private String prompt(String message) {
        System.out.print(color("  " + message + ": ", CYAN));
        System.out.flush();
        return readLine();
    }

    /** Main loop: render → read → process → repeat */
    public void run(TaskApp app) {
        State state = State.initial(app);

        while (state.running()) {
            state = state.refreshWidth();
            render(state);

            String input = readLine();
            state = processInput(state, input);
        }

        render(state); // Final render with goodbye message
    }

    /** Process input and return new state */
    private State processInput(State state, String input) {
        if (
            input == null ||
            input.equalsIgnoreCase("q") ||
            input.equalsIgnoreCase("quit")
        ) {
            return state.stop();
        }

        String cmd = input.toLowerCase().trim();

        // Handle commands
        Result<State> result = switch (cmd) {
            case "a", "add" -> handleAdd(state);
            case "v", "view" -> handleView(state);
            case "s", "start" -> handleStart(state);
            case "c", "complete" -> handleComplete(state);
            case "d", "delete" -> handleDelete(state);
            case "" -> Result.ok(state); // Just refresh
            default -> {
                if (cmd.matches("\\d+")) {
                    yield Actions.quickAction(state, Integer.parseInt(cmd));
                }
                yield Result.ok(
                    state.withStatus("Unknown command: " + input, RED)
                );
            }
        };

        // Fold result back to state
        return result.fold(
            error -> state.withStatus("Error: " + error, RED),
            newState -> newState
        );
    }

    // ─────────────────────────────────────────────────────────────────
    //  Interactive handlers (minimal I/O, delegate to Actions)
    // ─────────────────────────────────────────────────────────────────

    private Result<State> handleAdd(State state) {
        System.out.println();
        String title = prompt("Title");
        if (title == null || title.isBlank()) {
            return Result.ok(
                state.withStatus("Cancelled - title required", YELLOW)
            );
        }
        String desc = prompt("Description (optional)");
        return Actions.create(state, title, desc != null ? desc : "");
    }

    private Result<State> handleView(State state) {
        List<Task> tasks = state.tasks();
        if (tasks.isEmpty()) {
            return Result.ok(state.withStatus("No tasks to view!", YELLOW));
        }

        System.out.println();
        System.out.println(color("  Select task to view:", DIM));
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String icon = switch (t.status()) {
                case TODO -> color(BULLET, YELLOW);
                case DOING -> color(ICON_FLAME, MAGENTA);
                case DONE -> color(CHECK, GREEN);
            };
            System.out.println(
                color("    [" + (i + 1) + "] ", BRIGHT_BLACK) + icon + " " + t.title()
            );
        }

        String input = prompt("Task number");
        if (input == null || input.isBlank()) {
            return Result.ok(state.withStatus("Cancelled", YELLOW));
        }

        try {
            int num = Integer.parseInt(input.trim());
            if (num < 1 || num > tasks.size()) {
                return Result.fail("Invalid task number");
            }
            Task task = tasks.get(num - 1);

            // Display task details
            System.out.println();
            int w = state.width() - 4;
            System.out.println(color("  " + repeat(BOX_HORIZONTAL, w), CYAN));
            System.out.println();

            // Title with status icon
            String statusIcon = switch (task.status()) {
                case TODO -> color(BULLET + " TODO", YELLOW, BOLD);
                case DOING -> color(ICON_FLAME + " DOING", MAGENTA, BOLD);
                case DONE -> color(CHECK + " DONE", GREEN, BOLD);
            };
            System.out.println("  " + color(task.title(), BRIGHT_WHITE, BOLD));
            System.out.println("  " + statusIcon);
            System.out.println();

            // Description
            if (task.description() != null && !task.description().isBlank()) {
                System.out.println(color("  Description:", DIM));
                // Word wrap description
                String desc = task.description();
                int maxWidth = w - 4;
                while (desc.length() > maxWidth) {
                    int breakPoint = desc.lastIndexOf(' ', maxWidth);
                    if (breakPoint <= 0) breakPoint = maxWidth;
                    System.out.println("    " + desc.substring(0, breakPoint));
                    desc = desc.substring(breakPoint).trim();
                }
                if (!desc.isEmpty()) {
                    System.out.println("    " + desc);
                }
            } else {
                System.out.println(color("  No description", DIM, ITALIC));
            }

            System.out.println();
            System.out.println(color("  " + repeat(BOX_HORIZONTAL, w), CYAN));
            System.out.println();
            prompt("Press Enter to continue");

            return Result.ok(state.withStatus("Viewed: " + task.title(), BLUE));
        } catch (NumberFormatException e) {
            return Result.fail("Invalid number");
        }
    }

    private Result<State> handleStart(State state) {
        List<Task> todo = state.byStatus(TaskStatus.TODO);
        if (todo.isEmpty()) {
            return Result.ok(
                state.withStatus("No TODO tasks to start!", YELLOW)
            );
        }

        System.out.println();
        System.out.println(color("  TODO tasks:", DIM));
        for (int i = 0; i < todo.size(); i++) {
            System.out.println(
                color("    [" + (i + 1) + "] ", BRIGHT_BLACK) +
                    todo.get(i).title()
            );
        }

        String input = prompt("Task number to start");
        if (input == null || input.isBlank()) {
            return Result.ok(state.withStatus("Cancelled", YELLOW));
        }

        try {
            int num = Integer.parseInt(input.trim());
            if (num < 1 || num > todo.size()) {
                return Result.fail("Invalid task number");
            }
            return Actions.start(state, todo.get(num - 1).id());
        } catch (NumberFormatException e) {
            return Result.fail("Invalid number");
        }
    }

    private Result<State> handleComplete(State state) {
        List<Task> doing = state.byStatus(TaskStatus.DOING);
        List<Task> completable = !doing.isEmpty()
            ? doing
            : state.byStatus(TaskStatus.TODO);

        if (completable.isEmpty()) {
            return Result.ok(state.withStatus("No tasks to complete!", YELLOW));
        }

        System.out.println();
        String label = !doing.isEmpty() ? "DOING tasks:" : "TODO tasks:";
        System.out.println(color("  " + label, DIM));
        for (int i = 0; i < completable.size(); i++) {
            System.out.println(
                color("    [" + (i + 1) + "] ", BRIGHT_BLACK) +
                    completable.get(i).title()
            );
        }

        String input = prompt("Task number to complete");
        if (input == null || input.isBlank()) {
            return Result.ok(state.withStatus("Cancelled", YELLOW));
        }

        try {
            int num = Integer.parseInt(input.trim());
            if (num < 1 || num > completable.size()) {
                return Result.fail("Invalid task number");
            }
            return Actions.complete(state, completable.get(num - 1).id());
        } catch (NumberFormatException e) {
            return Result.fail("Invalid number");
        }
    }

    private Result<State> handleDelete(State state) {
        List<Task> tasks = state.tasks();
        if (tasks.isEmpty()) {
            return Result.ok(state.withStatus("No tasks to delete!", YELLOW));
        }

        System.out.println();
        System.out.println(color("  All tasks:", DIM));
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String icon = t.completed()
                ? color(CHECK, GREEN)
                : color(BULLET, YELLOW);
            System.out.println(
                color("    [" + (i + 1) + "] ", BRIGHT_BLACK) +
                    icon +
                    " " +
                    t.title()
            );
        }

        String input = prompt("Task number to delete");
        if (input == null || input.isBlank()) {
            return Result.ok(state.withStatus("Cancelled", YELLOW));
        }

        try {
            int num = Integer.parseInt(input.trim());
            if (num < 1 || num > tasks.size()) {
                return Result.fail("Invalid task number");
            }
            return Actions.delete(state, tasks.get(num - 1).id());
        } catch (NumberFormatException e) {
            return Result.fail("Invalid number");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        TaskApp app = TaskApp.withInMemoryRepo();

        // Sample data in different states
        app.createTask(
            "Learn HexaFun",
            "Study the fluent DSL and port registry"
        );
        app.createTask("Write tests", "Ensure everything works correctly");

        Result<Task> tui = app.createTask(
            "Build a TUI",
            "Create a terminal user interface"
        );
        tui.map(t -> app.startTask(t.id()));

        Result<Task> docs = app.createTask(
            "Read the docs",
            "Check out the documentation"
        );
        docs.map(t ->
            app.startTask(t.id()).map(started -> app.completeTask(started.id()))
        );

        new TasksTUI().run(app);
    }
}
