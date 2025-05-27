package com.guinetik.hexafun.testing;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HexaTest framework.
 */
public class HexaTestTest {
    
    private HexaApp app;
    
    @BeforeEach
    void setUp() {
        // Create a test app with some use cases
        app = HexaFun.dsl()
            .useCase("echo")
                .to(input -> input)
                .and()
            .useCase("validate")
                .from(input -> {
                    String str = (String) input;
                    if (str == null || str.isEmpty()) {
                        return Result.fail("Input cannot be empty");
                    }
                    return Result.ok(str);
                })
                .to(str -> Result.ok("Valid: " + str))
                .and()
            .useCase("throws")
                .to(input -> {
                    throw new RuntimeException("Test exception");
                })
                .and()
            .build();
    }
    
    @Test
    void testExpectOk() {
        // Test a use case with expectOk
        HexaTest.forApp(app)
            .useCase("echo")
            .with("Hello")
            .expectOk(result -> {
                assertEquals("Hello", result);
            });
    }
    
    @Test
    void testExpectWithPredicate() {
        // Test a use case with expect predicate
        HexaTest.forApp(app)
            .useCase("echo")
            .with("Hello")
            .expect(result -> "Hello".equals(result), "result equal to 'Hello'");
        
        // Test with failing predicate
        assertThrows(AssertionError.class, () -> {
            HexaTest.forApp(app)
                .useCase("echo")
                .with("Hello")
                .expect(result -> "Wrong".equals(result), "result equal to 'Wrong'");
        });
    }
    
    @Test
    void testExpectFailure() {
        // Test a use case with expectFailure
        HexaTest.forApp(app)
            .useCase("validate")
            .with("")
            .expectFailure(error -> {
                assertEquals("Input cannot be empty", error);
            });
    }
    
    @Test
    void testExpectException() {
        // Test a use case with expectException
        HexaTest.forApp(app)
            .useCase("throws")
            .with("anything")
            .expectException(RuntimeException.class);
        
        // Test with wrong exception type
        assertThrows(AssertionError.class, () -> {
            HexaTest.forApp(app)
                .useCase("throws")
                .with("anything")
                .expectException(IllegalArgumentException.class);
        });
    }
    
    @Test
    void testMap() {
        // Test mapping the result
        HexaTest.forApp(app)
            .useCase("echo")
            .with("Hello")
            .map(result -> ((String) result).length())
            .expectOk(length -> {
                assertEquals(5, length);
            });
    }
    
    @Test
    void testHasUseCase() {
        // Test asserting that a use case exists
        HexaTest hexaTest = HexaTest.forApp(app);
        
        // Should pass
        hexaTest.hasUseCase("echo");
        hexaTest.hasUseCase("validate");
        hexaTest.hasUseCase("throws");
        
        // Should fail
        assertThrows(AssertionError.class, () -> hexaTest.hasUseCase("nonexistent"));
    }
    
    @Test
    void testDoesNotHaveUseCase() {
        // Test asserting that a use case does not exist
        HexaTest hexaTest = HexaTest.forApp(app);
        
        // Should pass
        hexaTest.doesNotHaveUseCase("nonexistent");
        
        // Should fail
        assertThrows(AssertionError.class, () -> hexaTest.doesNotHaveUseCase("echo"));
    }
    
    @Test
    void testShortMethodName() {
        // Test the shorter test() method
        HexaTest.forApp(app)
            .test("echo") // Same as useCase("echo")
            .with("Test")
            .expectOk(result -> {
                assertEquals("Test", result);
            });
    }
}
