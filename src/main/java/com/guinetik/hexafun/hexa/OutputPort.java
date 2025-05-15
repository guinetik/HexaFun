package com.guinetik.hexafun.hexa;

/**
 * Secondary/Driven port - the way your application interacts with external systems.
 * Examples: repositories, email senders, payment gateways.
 * @param <I> Input type - what your application provides to the external system
 * @param <O> Output type - what the external system responds with
 */
@FunctionalInterface
public interface OutputPort<I, O> {
    /**
     * Apply an operation to the input and produce an output.
     * @param input The input to process
     * @return The output response
     */
    O apply(I input);
}
