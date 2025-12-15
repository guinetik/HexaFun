package com.guinetik.hexafun.testing;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;
import com.guinetik.hexafun.hexa.UseCaseKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HexaTest framework.
 */
@DisplayName("HexaTest")
public class HexaTestTest {

    // Type-safe keys
    static final UseCaseKey<Object, Object> ECHO = UseCaseKey.of("echo");
    static final UseCaseKey<String, Result<String>> VALIDATE = UseCaseKey.of("validate");
    static final UseCaseKey<Object, Object> THROWS = UseCaseKey.of("throws");

    private HexaApp app;

    @BeforeEach
    void setUp() {
        app = HexaFun.dsl()
            .useCase(ECHO)
                .handle(input -> input)
            .useCase(VALIDATE)
                .validate(input -> {
                    if (input == null || input.isEmpty()) {
                        return Result.fail("Input cannot be empty");
                    }
                    return Result.ok(input);
                })
                .handle(str -> Result.ok("Valid: " + str))
            .useCase(THROWS)
                .handle(input -> {
                    throw new RuntimeException("Test exception");
                })
            .build();
    }

    @Test
    @DisplayName("expectOk should pass when result matches")
    void testExpectOk() {
        app.test(ECHO)
            .with("Hello")
            .expectOk(result -> {
                assertEquals("Hello", result);
            });
    }

    @Test
    @DisplayName("expect with predicate should pass when predicate returns true")
    void testExpectWithPredicate() {
        app.test(ECHO)
            .with("Hello")
            .expect(result -> "Hello".equals(result), "result equal to 'Hello'");
    }

    @Test
    @DisplayName("expect with predicate should fail when predicate returns false")
    void testExpectPredicateFails() {
        assertThrows(AssertionError.class, () -> {
            app.test(ECHO)
                .with("Hello")
                .expect(result -> "Wrong".equals(result), "result equal to 'Wrong'");
        });
    }

    @Test
    @DisplayName("expectFailure should pass when validation fails")
    void testExpectFailure() {
        app.test(VALIDATE)
            .with("")
            .expectFailure(error -> {
                assertEquals("Input cannot be empty", error);
            });
    }

    @Test
    @DisplayName("expectException should pass when exception thrown")
    void testExpectException() {
        app.test(THROWS)
            .with("anything")
            .expectException(RuntimeException.class);
    }

    @Test
    @DisplayName("expectException should fail with wrong exception type")
    void testExpectExceptionWrongType() {
        assertThrows(AssertionError.class, () -> {
            app.test(THROWS)
                .with("anything")
                .expectException(IllegalArgumentException.class);
        });
    }

    @Test
    @DisplayName("map should transform result")
    void testMap() {
        app.test(ECHO)
            .with("Hello")
            .map(result -> ((String) result).length())
            .expectOk(length -> {
                assertEquals(5, length);
            });
    }

    @Test
    @DisplayName("hasUseCase should pass for registered use cases")
    void testHasUseCase() {
        HexaTest hexaTest = HexaTest.forApp(app);

        hexaTest.hasUseCase("echo");
        hexaTest.hasUseCase("validate");
        hexaTest.hasUseCase("throws");
    }

    @Test
    @DisplayName("hasUseCase should fail for non-existent use case")
    void testHasUseCaseFails() {
        HexaTest hexaTest = HexaTest.forApp(app);
        assertThrows(AssertionError.class, () -> hexaTest.hasUseCase("nonexistent"));
    }

    @Test
    @DisplayName("doesNotHaveUseCase should pass for non-existent use case")
    void testDoesNotHaveUseCase() {
        HexaTest hexaTest = HexaTest.forApp(app);
        hexaTest.doesNotHaveUseCase("nonexistent");
    }

    @Test
    @DisplayName("doesNotHaveUseCase should fail for existing use case")
    void testDoesNotHaveUseCaseFails() {
        HexaTest hexaTest = HexaTest.forApp(app);
        assertThrows(AssertionError.class, () -> hexaTest.doesNotHaveUseCase("echo"));
    }
}
