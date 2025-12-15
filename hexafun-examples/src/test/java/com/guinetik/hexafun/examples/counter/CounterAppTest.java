package com.guinetik.hexafun.examples.counter;

import com.guinetik.hexafun.fun.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.guinetik.hexafun.examples.counter.CounterUseCases.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CounterApp (v2 DSL)")
public class CounterAppTest {

    private CounterApp app;

    @BeforeEach
    void setUp() {
        app = new CounterApp();
    }

    @Nested
    @DisplayName("increment")
    class IncrementTest {

        @Test
        @DisplayName("should increment counter by 1")
        void shouldIncrement() {
            Counter counter = Counter.of(5);

            Result<Counter> result = app.increment(counter);

            assertTrue(result.isSuccess());
            assertEquals(6, result.get().value());
        }

        @Test
        @DisplayName("should fail when counter is null")
        void shouldFailWhenCounterNull() {
            Result<Counter> result = app.increment(null);

            assertTrue(result.isFailure());
            assertEquals("Counter cannot be null", result.error());
        }

        @Test
        @DisplayName("should work with zero")
        void shouldWorkWithZero() {
            Counter counter = Counter.zero();

            Result<Counter> result = app.increment(counter);

            assertTrue(result.isSuccess());
            assertEquals(1, result.get().value());
        }

        @Test
        @DisplayName("should work with negative values")
        void shouldWorkWithNegative() {
            Counter counter = Counter.of(-5);

            Result<Counter> result = app.increment(counter);

            assertTrue(result.isSuccess());
            assertEquals(-4, result.get().value());
        }
    }

    @Nested
    @DisplayName("decrement")
    class DecrementTest {

        @Test
        @DisplayName("should decrement counter by 1")
        void shouldDecrement() {
            Counter counter = Counter.of(5);

            Result<Counter> result = app.decrement(counter);

            assertTrue(result.isSuccess());
            assertEquals(4, result.get().value());
        }

        @Test
        @DisplayName("should fail when counter is null")
        void shouldFailWhenCounterNull() {
            Result<Counter> result = app.decrement(null);

            assertTrue(result.isFailure());
            assertEquals("Counter cannot be null", result.error());
        }

        @Test
        @DisplayName("should go negative")
        void shouldGoNegative() {
            Counter counter = Counter.zero();

            Result<Counter> result = app.decrement(counter);

            assertTrue(result.isSuccess());
            assertEquals(-1, result.get().value());
        }
    }

    @Nested
    @DisplayName("add")
    class AddTest {

        @Test
        @DisplayName("should add positive amount")
        void shouldAddPositive() {
            Counter counter = Counter.of(10);

            Result<Counter> result = app.add(counter, 5);

            assertTrue(result.isSuccess());
            assertEquals(15, result.get().value());
        }

        @Test
        @DisplayName("should add negative amount")
        void shouldAddNegative() {
            Counter counter = Counter.of(10);

            Result<Counter> result = app.add(counter, -3);

            assertTrue(result.isSuccess());
            assertEquals(7, result.get().value());
        }

        @Test
        @DisplayName("should fail when counter is null")
        void shouldFailWhenCounterNull() {
            Result<Counter> result = app.add(null, 5);

            assertTrue(result.isFailure());
            assertEquals("Counter cannot be null", result.error());
        }

        @Test
        @DisplayName("should fail when amount exceeds upper bound")
        void shouldFailWhenAmountTooLarge() {
            Counter counter = Counter.of(10);

            Result<Counter> result = app.add(counter, 101);

            assertTrue(result.isFailure());
            assertEquals("Amount must be between -100 and 100", result.error());
        }

        @Test
        @DisplayName("should fail when amount below lower bound")
        void shouldFailWhenAmountTooSmall() {
            Counter counter = Counter.of(10);

            Result<Counter> result = app.add(counter, -101);

            assertTrue(result.isFailure());
            assertEquals("Amount must be between -100 and 100", result.error());
        }

        @Test
        @DisplayName("should pass at upper boundary (100)")
        void shouldPassAtUpperBoundary() {
            Counter counter = Counter.of(0);

            Result<Counter> result = app.add(counter, 100);

            assertTrue(result.isSuccess());
            assertEquals(100, result.get().value());
        }

        @Test
        @DisplayName("should pass at lower boundary (-100)")
        void shouldPassAtLowerBoundary() {
            Counter counter = Counter.of(0);

            Result<Counter> result = app.add(counter, -100);

            assertTrue(result.isSuccess());
            assertEquals(-100, result.get().value());
        }

        @Test
        @DisplayName("validator chain: first validator fails")
        void validatorChainFirstFails() {
            // null counter should fail at first validator (before amount check)
            Result<Counter> result = app.add(null, 50);

            assertTrue(result.isFailure());
            assertEquals("Counter cannot be null", result.error());
        }

        @Test
        @DisplayName("validator chain: second validator fails")
        void validatorChainSecondFails() {
            // valid counter but invalid amount - fails at second validator
            Counter counter = Counter.of(10);

            Result<Counter> result = app.add(counter, 500);

            assertTrue(result.isFailure());
            assertEquals("Amount must be between -100 and 100", result.error());
        }
    }

    @Nested
    @DisplayName("type-safe keys")
    class TypeSafeKeysTest {

        @Test
        @DisplayName("should register all use cases")
        void shouldRegisterAllUseCases() {
            var registered = app.getApp().registeredUseCases();

            assertTrue(registered.contains(INCREMENT.name()));
            assertTrue(registered.contains(DECREMENT.name()));
            assertTrue(registered.contains(ADD.name()));
            assertEquals(3, registered.size());
        }

        @Test
        @DisplayName("should invoke via type-safe key")
        void shouldInvokeViaKey() {
            Counter counter = Counter.of(5);
            CounterInputs.IncrementInput input = new CounterInputs.IncrementInput(counter);

            Result<Counter> result = app.getApp().invoke(INCREMENT, input);

            assertTrue(result.isSuccess());
            assertEquals(6, result.get().value());
        }
    }

    @Nested
    @DisplayName("chaining operations")
    class ChainingTest {

        @Test
        @DisplayName("should chain multiple operations")
        void shouldChainOperations() {
            Counter counter = Counter.zero();

            Result<Counter> r1 = app.increment(counter);
            assertTrue(r1.isSuccess());

            Result<Counter> r2 = app.add(r1.get(), 10);
            assertTrue(r2.isSuccess());

            Result<Counter> r3 = app.decrement(r2.get());
            assertTrue(r3.isSuccess());

            assertEquals(10, r3.get().value()); // 0 + 1 + 10 - 1 = 10
        }
    }
}
