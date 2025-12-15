package com.guinetik.hexafun.examples.counter;

import com.guinetik.hexafun.fun.Result;
import com.guinetik.hexafun.hexa.UseCaseKey;
import com.guinetik.hexafun.examples.counter.CounterInputs.*;

/**
 * Type-safe use case keys for counter operations.
 *
 * <p>Define all use case signatures in one place.
 * Provides compile-time type safety for invocation.
 */
public interface CounterUseCases {

    UseCaseKey<IncrementInput, Result<Counter>> INCREMENT =
            UseCaseKey.of("increment");

    UseCaseKey<DecrementInput, Result<Counter>> DECREMENT =
            UseCaseKey.of("decrement");

    UseCaseKey<AddInput, Result<Counter>> ADD =
            UseCaseKey.of("add");
}
