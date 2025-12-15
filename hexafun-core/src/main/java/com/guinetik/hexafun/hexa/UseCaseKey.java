package com.guinetik.hexafun.hexa;

import java.util.Objects;

/**
 * Type-safe key for use case registration and invocation.
 * Provides compile-time type checking instead of string-based dispatch.
 *
 * <p>Usage:
 * <pre class="language-java">{@code
 * // Define keys as constants
 * public interface MyUseCases {
 *     UseCaseKey<CreateInput, Result<Entity>> CREATE = UseCaseKey.of("create");
 *     UseCaseKey<String, Result<Entity>> FIND = UseCaseKey.of("find");
 * }
 *
 * // Use in DSL
 * HexaFun.dsl()
 *     .useCase(MyUseCases.CREATE)
 *         .validate(...)
 *         .handle(...)
 *     .build();
 *
 * // Type-safe invocation
 * Result<Entity> result = app.invoke(MyUseCases.CREATE, input);
 * }</pre>
 *
 * @param <I> The input type of the use case
 * @param <O> The output type of the use case
 */
public final class UseCaseKey<I, O> {

    private final String name;

    private UseCaseKey(String name) {
        this.name = Objects.requireNonNull(
            name,
            "Use case name cannot be null"
        );
    }

    /**
     * Create a new type-safe use case key.
     *
     * @param name The unique name for this use case
     * @param <I> The input type
     * @param <O> The output type
     * @return A new UseCaseKey instance
     */
    public static <I, O> UseCaseKey<I, O> of(String name) {
        return new UseCaseKey<>(name);
    }

    /**
     * Get the string name of this use case key.
     * @return The use case name
     */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UseCaseKey<?, ?> that = (UseCaseKey<?, ?>) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "UseCaseKey(" + name + ")";
    }
}
