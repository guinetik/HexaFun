package com.guinetik.hexafun.examples.counter;

import com.guinetik.hexafun.fun.Result;
import com.guinetik.hexafun.examples.counter.CounterInputs.*;

/**
 * Validation functions for counter operations.
 *
 * <p>Each validator is a pure function: Input -> Result&lt;Input&gt;
 * Can be composed/chained in the DSL.
 */
public final class CounterValidators {

    private CounterValidators() {}

    /**
     * Validates that the counter in IncrementInput is not null.
     */
    public static Result<IncrementInput> validateIncrement(IncrementInput input) {
        if (input == null) {
            return Result.fail("Input cannot be null");
        }
        if (input.counter() == null) {
            return Result.fail("Counter cannot be null");
        }
        return Result.ok(input);
    }

    /**
     * Validates that the counter in DecrementInput is not null.
     */
    public static Result<DecrementInput> validateDecrement(DecrementInput input) {
        if (input == null) {
            return Result.fail("Input cannot be null");
        }
        if (input.counter() == null) {
            return Result.fail("Counter cannot be null");
        }
        return Result.ok(input);
    }

    /**
     * Validates that the counter in AddInput is not null.
     */
    public static Result<AddInput> validateAddCounter(AddInput input) {
        if (input == null) {
            return Result.fail("Input cannot be null");
        }
        if (input.counter() == null) {
            return Result.fail("Counter cannot be null");
        }
        return Result.ok(input);
    }

    /**
     * Validates that the amount in AddInput is within bounds [-100, 100].
     */
    public static Result<AddInput> validateAddAmount(AddInput input) {
        if (input.amount() < -100 || input.amount() > 100) {
            return Result.fail("Amount must be between -100 and 100");
        }
        return Result.ok(input);
    }
}
