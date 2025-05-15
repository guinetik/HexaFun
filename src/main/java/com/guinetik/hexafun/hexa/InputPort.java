package com.guinetik.hexafun.hexa;

/**
 * Primary/Driving port (or port adapter) - the way actors interact with your application.
 * Examples: REST controllers, CLI interfaces, message consumers.
 * @param <I> Input type - what comes into your application
 * @param <O> Output type - what your application responds with
 */
@FunctionalInterface
public interface InputPort<I, O> {
    /**
     * Handle an input request and produce an output response.
     * @param input The input to process
     * @return The output response
     */
    O handle(I input);
}
