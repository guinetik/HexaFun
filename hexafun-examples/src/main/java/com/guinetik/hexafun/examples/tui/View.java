package com.guinetik.hexafun.examples.tui;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A View is a pure function from State to String.
 *
 * <p>Views are the building blocks of functional TUIs. They:
 * <ul>
 *   <li>Take immutable state as input</li>
 *   <li>Return a String representation</li>
 *   <li>Have no side effects</li>
 *   <li>Compose via {@link #andThen(View)}</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre class="language-java">{@code
 * View<AppState> header = state -> "=== " + state.title() + " ===\n";
 * View<AppState> content = state -> state.items().stream()...;
 * View<AppState> footer = state -> "\nTotal: " + state.count();
 *
 * // Compose views
 * View<AppState> screen = header
 *     .andThen(content)
 *     .andThen(footer);
 *
 * // Render (single side effect)
 * System.out.print(screen.apply(state));
 * }</pre>
 *
 * @param <S> The state type this view renders
 */
@FunctionalInterface
public interface View<S> extends Function<S, String> {
    /**
     * Compose this view with another, concatenating their outputs.
     *
     * @param next The view to append after this one
     * @return A new view that renders both in sequence
     */
    default View<S> andThen(View<S> next) {
        return state -> this.apply(state) + next.apply(state);
    }

    /**
     * Compose with a separator between views.
     *
     * @param separator String to insert between views
     * @param next The view to append
     * @return A new composed view
     */
    default View<S> andThen(String separator, View<S> next) {
        return state -> this.apply(state) + separator + next.apply(state);
    }

    /**
     * Conditional view - renders one of two views based on predicate.
     *
     * @param condition Predicate to test against state
     * @param ifTrue View to render if condition is true
     * @param ifFalse View to render if condition is false
     * @return A new conditional view
     */
    static <S> View<S> when(
        Predicate<S> condition,
        View<S> ifTrue,
        View<S> ifFalse
    ) {
        return state ->
            condition.test(state) ? ifTrue.apply(state) : ifFalse.apply(state);
    }

    /**
     * Conditional view with empty fallback.
     *
     * @param condition Predicate to test against state
     * @param view View to render if condition is true
     * @return A new conditional view (empty if false)
     */
    static <S> View<S> when(Predicate<S> condition, View<S> view) {
        return when(condition, view, empty());
    }

    /**
     * Empty view - renders nothing.
     *
     * @return A view that returns empty string
     */
    static <S> View<S> empty() {
        return state -> "";
    }

    /**
     * Literal view - always renders the same string.
     *
     * @param text The literal string to render
     * @return A view that ignores state and returns the literal
     */
    static <S> View<S> of(String text) {
        return state -> text;
    }

    /**
     * Newline view - renders a blank line.
     *
     * @return A view that returns a newline
     */
    static <S> View<S> newline() {
        return state -> "\n";
    }

    /**
     * Create a view from a function.
     *
     * @param fn Function to convert to a View
     * @return The function as a View
     */
    static <S> View<S> from(Function<S, String> fn) {
        return fn::apply;
    }

    /**
     * Combine multiple views into one.
     *
     * @param views Views to combine
     * @return A single view that renders all in sequence
     */
    @SafeVarargs
    static <S> View<S> compose(View<S>... views) {
        return state -> {
            StringBuilder sb = new StringBuilder();
            for (View<S> view : views) {
                sb.append(view.apply(state));
            }
            return sb.toString();
        };
    }
}
