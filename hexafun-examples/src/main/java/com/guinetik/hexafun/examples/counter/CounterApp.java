package com.guinetik.hexafun.examples.counter;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;
import com.guinetik.hexafun.examples.counter.CounterInputs.*;

import static com.guinetik.hexafun.examples.counter.CounterUseCases.*;
import static com.guinetik.hexafun.examples.counter.CounterValidators.*;

/**
 * Counter application demonstrating the improved HexaFun DSL.
 *
 * <p>Key improvements showcased:
 * <ul>
 *   <li>Type-safe keys (UseCaseKey) instead of strings</li>
 *   <li>Cleaner syntax: validate/handle instead of from/to</li>
 *   <li>Implicit closure: no .and() chaining needed</li>
 *   <li>Validator chaining: multiple .validate() calls</li>
 * </ul>
 */
public class CounterApp {

    private final HexaApp app;

    public CounterApp() {
        this.app = HexaFun.dsl()

            // Increment: single validator
            .useCase(INCREMENT)
                .validate(CounterValidators::validateIncrement)
                .handle(input -> Result.ok(input.counter().increment()))

            // Decrement: single validator
            .useCase(DECREMENT)
                .validate(CounterValidators::validateDecrement)
                .handle(input -> Result.ok(input.counter().decrement()))

            // Add: chained validators (counter not null + amount in range)
            .useCase(ADD)
                .validate(CounterValidators::validateAddCounter)
                .validate(CounterValidators::validateAddAmount)
                .handle(input -> Result.ok(input.counter().add(input.amount())))

            .build();
    }

    /**
     * Increment a counter.
     */
    public Result<Counter> increment(Counter counter) {
        return app.invoke(INCREMENT, new IncrementInput(counter));
    }

    /**
     * Decrement a counter.
     */
    public Result<Counter> decrement(Counter counter) {
        return app.invoke(DECREMENT, new DecrementInput(counter));
    }

    /**
     * Add amount to a counter.
     */
    public Result<Counter> add(Counter counter, int amount) {
        return app.invoke(ADD, new AddInput(counter, amount));
    }

    /**
     * Get the underlying HexaApp for testing.
     */
    public HexaApp getApp() {
        return app;
    }

    // ----- Demo -----

    public static void main(String[] args) {
        CounterApp app = new CounterApp();
        Counter counter = Counter.zero();

        System.out.println("Starting: " + counter);

        // Increment
        Result<Counter> r1 = app.increment(counter);
        System.out.println("After increment: " + r1.get());

        // Add 10
        Result<Counter> r2 = app.add(r1.get(), 10);
        System.out.println("After add(10): " + r2.get());

        // Try invalid add (amount too large)
        Result<Counter> r3 = app.add(r2.get(), 500);
        System.out.println("After add(500): " + (r3.isSuccess() ? r3.get() : "FAILED: " + r3.error()));

        // Decrement
        Result<Counter> r4 = app.decrement(r2.get());
        System.out.println("After decrement: " + r4.get());

        // Try null counter
        Result<Counter> r5 = app.increment(null);
        System.out.println("Increment null: " + (r5.isSuccess() ? r5.get() : "FAILED: " + r5.error()));
    }
}
