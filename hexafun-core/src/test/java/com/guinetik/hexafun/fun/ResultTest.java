package com.guinetik.hexafun.fun;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Result type.
 */
public class ResultTest {
    
    @Test
    void testSuccessResult() {
        Result<String> result = Result.ok("Success");
        
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("Success", result.get());
        assertThrows(IllegalStateException.class, result::error);
    }
    
    @Test
    void testFailureResult() {
        Result<String> result = Result.fail("Error message");
        
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals("Error message", result.error());
        assertThrows(IllegalStateException.class, result::get);
    }
    
    @Test
    void testMap() {
        Result<Integer> success = Result.ok(5);
        Result<String> mappedSuccess = success.map(i -> "Number: " + i);
        
        assertTrue(mappedSuccess.isSuccess());
        assertEquals("Number: 5", mappedSuccess.get());
        
        Result<Integer> failure = Result.fail("Invalid number");
        Result<String> mappedFailure = failure.map(i -> "Number: " + i);
        
        assertTrue(mappedFailure.isFailure());
        assertEquals("Invalid number", mappedFailure.error());
    }
    
    @Test
    void testFlatMap() {
        Function<Integer, Result<String>> successFn = i -> Result.ok("Number: " + i);
        Function<Integer, Result<String>> failureFn = i -> Result.fail("Error for: " + i);
        
        Result<Integer> success = Result.ok(5);
        
        // Success + Success path
        Result<String> successResult = success.flatMap(successFn);
        assertTrue(successResult.isSuccess());
        assertEquals("Number: 5", successResult.get());
        
        // Success + Failure path
        Result<String> mixedResult = success.flatMap(failureFn);
        assertTrue(mixedResult.isFailure());
        assertEquals("Error for: 5", mixedResult.error());
        
        // Failure + Success path (should short-circuit)
        Result<Integer> failure = Result.fail("Initial failure");
        Result<String> failureResult = failure.flatMap(successFn);
        assertTrue(failureResult.isFailure());
        assertEquals("Initial failure", failureResult.error());
    }
    
    @Test
    void testFold() {
        Result<Integer> success = Result.ok(42);
        Result<Integer> failure = Result.fail("Error");
        
        // Fold success
        String successFolded = success.fold(
            error -> "Failed: " + error,
            value -> "Success: " + value
        );
        assertEquals("Success: 42", successFolded);
        
        // Fold failure
        String failureFolded = failure.fold(
            error -> "Failed: " + error,
            value -> "Success: " + value
        );
        assertEquals("Failed: Error", failureFolded);
    }
    
    @Test
    void testLazyEvaluation() {
        AtomicBoolean mapCalled = new AtomicBoolean(false);
        
        Result<Integer> failure = Result.fail("Test error");
        failure.map(value -> {
            mapCalled.set(true);
            return value * 2;
        });
        
        // The map function should not be called for failures
        assertFalse(mapCalled.get());
    }
}
