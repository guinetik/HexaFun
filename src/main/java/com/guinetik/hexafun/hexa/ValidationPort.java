package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.fun.Result;

/**
 * Port for validating input data before processing by a use case.
 * @param <I> Input type to validate
 */
@FunctionalInterface
public interface ValidationPort<I> {
    /**
     * Validate input data and return a result.
     * @param input The input to validate
     * @return A Result containing either the validated input or an error
     */
    Result<I> validate(I input);
}
