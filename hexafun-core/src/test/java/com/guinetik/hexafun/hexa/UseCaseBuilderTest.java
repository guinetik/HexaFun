package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the core HexaFun DSL and use case building.
 */
public class UseCaseBuilderTest {
    
    @Test
    void testSimpleUseCase() {
        // Build a simple use case
        HexaApp app = HexaFun.dsl()
            .useCase("double")
                .to(i -> (Integer) i * 2)
                .and()
            .build();
        
        // Test it
        Integer result = app.invoke("double", 5);
        assertEquals(10, result);
    }
    
    @Test
    void testUseCaseWithValidation() {
        // Build a use case with validation
        HexaApp app = HexaFun.dsl()
            .useCase("divide")
                .from(args -> {
                    int[] numbers = (int[]) args;
                    if (numbers[1] == 0) {
                        return Result.fail("Cannot divide by zero");
                    }
                    return Result.ok(numbers);
                })
                .to(numbers -> {
                    int[] vals = (int[]) numbers;
                    return Result.ok(vals[0] / vals[1]);
                })
                .and()
            .build();
        
        // Test successful case
        Result<Integer> successResult = app.invoke("divide", new int[]{10, 2});
        assertTrue(successResult.isSuccess());
        assertEquals(5, successResult.get());
        
        // Test failure case
        Result<Integer> failureResult = app.invoke("divide", new int[]{10, 0});
        assertTrue(failureResult.isFailure());
        assertEquals("Cannot divide by zero", failureResult.error());
    }
    
    @Test
    void testMultipleUseCases() {
        // Build multiple use cases
        HexaApp app = HexaFun.dsl()
            .useCase("add")
                .to(args -> {
                    int[] vals = (int[]) args;
                    return vals[0] + vals[1];
                })
                .and()
            .useCase("subtract")
                .to(args -> {
                    int[] vals = (int[]) args;
                    return vals[0] - vals[1];
                })
                .and()
            .useCase("multiply")
                .to(args -> {
                    int[] vals = (int[]) args;
                    return vals[0] * vals[1];
                })
                .and()
            .build();
        
        // Test add
        assertEquals(15, (int) app.invoke("add", new int[]{10, 5}));
        
        // Test subtract
        assertEquals(5, (int) app.invoke("subtract", new int[]{10, 5}));
        
        // Test multiply
        assertEquals(50, (int) app.invoke("multiply", new int[]{10, 5}));
    }
    
    @Test
    void testExceptionHandling() {
        // Build a use case that might throw an exception
        HexaApp app = HexaFun.dsl()
            .useCase("risky")
                .to(input -> {
                    if ("throw".equals(input)) {
                        throw new RuntimeException("Deliberate test exception");
                    }
                    return "Success: " + input;
                })
                .and()
            .build();
        
        // Test successful case
        assertEquals("Success: hello", app.invoke("risky", "hello"));
        
        // Test exception case
        assertThrows(RuntimeException.class, () -> app.invoke("risky", "throw"));
    }
    
    @Test
    void testRegisteredUseCases() {
        // Build app with use cases
        HexaApp app = HexaFun.dsl()
            .useCase("one").to(i -> i).and()
            .useCase("two").to(i -> i).and()
            .useCase("three").to(i -> i).and()
            .build();
        
        // Test registered use cases
        var useCases = app.registeredUseCases();
        assertEquals(3, useCases.size());
        assertTrue(useCases.contains("one"));
        assertTrue(useCases.contains("two"));
        assertTrue(useCases.contains("three"));
    }
}
