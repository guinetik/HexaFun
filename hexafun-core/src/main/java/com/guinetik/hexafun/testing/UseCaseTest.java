package com.guinetik.hexafun.testing;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.fun.Result;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A fluent API for testing HexaFun use cases.
 * @param <I> The input type of the use case
 * @param <O> The output type of the use case
 */
public class UseCaseTest<I, O> {
    private final HexaApp app;
    private final String useCaseName;
    private I input;
    private O output;
    private Exception exception;
    private boolean executed = false;

    UseCaseTest(HexaApp app, String useCaseName) {
        this.app = app;
        this.useCaseName = useCaseName;
    }

    /**
     * Specify the input for the use case.
     * @param input The input to provide to the use case
     * @return This test instance
     */
    public UseCaseTest<I, O> with(I input) {
        this.input = input;
        return this;
    }

    /**
     * Execute the use case and verify the result is successful.
     * @param verifier A consumer that receives the result for verification
     * @return This test instance
     */
    public UseCaseTest<I, O> expectOk(Consumer<O> verifier) {
        executeIfNeeded();
        if (exception != null) {
            throw new AssertionError("Expected successful result but got exception: " + exception.getMessage(), exception);
        }
        
        if (output instanceof Result) {
            Result<?> result = (Result<?>) output;
            if (result.isFailure()) {
                throw new AssertionError("Expected successful result but got failure: " + result.error());
            }
            @SuppressWarnings("unchecked")
            O unwrapped = (O) result.get();
            verifier.accept(unwrapped);
        } else {
            verifier.accept(output);
        }
        
        return this;
    }

    /**
     * Execute the use case and verify the result is a failure.
     * @param errorVerifier A consumer that receives the error message for verification
     * @return This test instance
     */
    public UseCaseTest<I, O> expectFailure(Consumer<String> errorVerifier) {
        executeIfNeeded();
        if (exception != null) {
            errorVerifier.accept(exception.getMessage());
            return this;
        }
        
        if (output instanceof Result) {
            Result<?> result = (Result<?>) output;
            if (result.isSuccess()) {
                throw new AssertionError("Expected failure but got successful result: " + result.get());
            }
            errorVerifier.accept(result.error());
        } else {
            throw new AssertionError("Expected Result type but got: " + output.getClass().getName());
        }
        
        return this;
    }

    /**
     * Execute the use case and verify the result matches a predicate.
     * @param predicate A predicate to test the result
     * @param description Description of what the predicate checks
     * @return This test instance
     */
    public UseCaseTest<I, O> expect(Predicate<O> predicate, String description) {
        executeIfNeeded();
        if (exception != null) {
            throw new AssertionError("Expected result but got exception: " + exception.getMessage(), exception);
        }
        
        if (!predicate.test(output)) {
            throw new AssertionError("Expected " + description + " but was not satisfied by " + output);
        }
        
        return this;
    }

    /**
     * Execute the use case and verify it throws a specific exception.
     * @param exceptionClass Expected exception class
     * @return This test instance
     */
    public UseCaseTest<I, O> expectException(Class<? extends Exception> exceptionClass) {
        executeIfNeeded();
        if (exception == null) {
            throw new AssertionError("Expected exception of type " + exceptionClass.getName() + " but no exception was thrown");
        }
        
        if (!exceptionClass.isInstance(exception)) {
            throw new AssertionError("Expected exception of type " + exceptionClass.getName() 
                + " but got " + exception.getClass().getName(), exception);
        }
        
        return this;
    }

    /**
     * Map the result using a transformer function for further verification.
     * @param mapper Function to transform the result
     * @param <T> Target type
     * @return A new test instance with the transformed result
     */
    public <T> UseCaseTest<I, T> map(Function<O, T> mapper) {
        executeIfNeeded();
        if (exception != null) {
            throw new AssertionError("Cannot map result: an exception occurred: " + exception.getMessage(), exception);
        }
        
        UseCaseTest<I, T> mappedTest = new UseCaseTest<>(app, useCaseName);
        mappedTest.input = this.input;
        mappedTest.executed = true;
        
        try {
            mappedTest.output = mapper.apply(output);
        } catch (Exception e) {
            mappedTest.exception = e;
        }
        
        return mappedTest;
    }

    /**
     * Execute the use case if it hasn't been executed yet.
     */
    private void executeIfNeeded() {
        if (executed) {
            return;
        }

        try {
            output = app.invokeByName(useCaseName, input);
        } catch (Exception e) {
            exception = e;
        }

        executed = true;
    }
}
