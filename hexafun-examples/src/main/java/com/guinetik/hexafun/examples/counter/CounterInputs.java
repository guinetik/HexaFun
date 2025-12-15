package com.guinetik.hexafun.examples.counter;

/**
 * Input types for counter operations.
 */
public final class CounterInputs {

    private CounterInputs() {}

    /**
     * Input for increment operation.
     */
    public record IncrementInput(Counter counter) {}

    /**
     * Input for decrement operation.
     */
    public record DecrementInput(Counter counter) {}

    /**
     * Input for add operation with amount validation.
     */
    public record AddInput(Counter counter, int amount) {}
}
