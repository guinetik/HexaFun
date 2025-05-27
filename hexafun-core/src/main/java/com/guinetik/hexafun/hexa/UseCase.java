package com.guinetik.hexafun.hexa;

/**
 * Core abstraction of business logic in Hexagonal Architecture.
 * Each use case represents a specific operation or workflow in your domain.
 * @param <I> Input type - the input to the use case
 * @param <O> Output type - the result of the use case
 */
@FunctionalInterface
public interface UseCase<I, O> {
    /**
     * Apply the use case logic to the input and produce an output.
     * @param input The input to process
     * @return The result of the use case
     */
    O apply(I input);
}
