package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HexaFun DSL.
 */
@DisplayName("UseCaseBuilder")
public class UseCaseBuilderTest {

    @Nested
    @DisplayName("handle() - direct handler")
    class HandleTests {

        @Test
        @DisplayName("should build use case with handle()")
        void shouldBuildWithHandle() {
            UseCaseKey<String, String> GREET = UseCaseKey.of("greet");

            HexaApp app = HexaFun.dsl()
                .useCase(GREET)
                    .handle(name -> "Hello, " + name + "!")
                .build();

            String result = app.invoke(GREET, "World");
            assertEquals("Hello, World!", result);
        }

        @Test
        @DisplayName("should pass method reference to handle()")
        void shouldAcceptMethodReference() {
            UseCaseKey<String, Integer> LENGTH = UseCaseKey.of("length");

            HexaApp app = HexaFun.dsl()
                .useCase(LENGTH)
                    .handle(String::length)
                .build();

            Integer result = app.invoke(LENGTH, "hello");
            assertEquals(5, result);
        }

        @Test
        @DisplayName("should handle exceptions from handler")
        void shouldHandleExceptions() {
            UseCaseKey<String, String> RISKY = UseCaseKey.of("risky");

            HexaApp app = HexaFun.dsl()
                .useCase(RISKY)
                    .handle(input -> {
                        if ("throw".equals(input)) {
                            throw new RuntimeException("Deliberate exception");
                        }
                        return "Success: " + input;
                    })
                .build();

            assertEquals("Success: hello", app.invoke(RISKY, "hello"));
            assertThrows(RuntimeException.class, () -> app.invoke(RISKY, "throw"));
        }
    }

    @Nested
    @DisplayName("validate().handle() - with validation")
    class ValidateHandleTests {

        @Test
        @DisplayName("should validate then handle on success")
        void shouldValidateThenHandle() {
            UseCaseKey<Integer, Result<Integer>> DOUBLE = UseCaseKey.of("double");

            HexaApp app = HexaFun.dsl()
                .useCase(DOUBLE)
                    .validate(i -> i != null ? Result.ok(i) : Result.fail("Input null"))
                    .handle(i -> Result.ok(i * 2))
                .build();

            Result<Integer> result = app.invoke(DOUBLE, 5);
            assertTrue(result.isSuccess());
            assertEquals(10, result.get());
        }

        @Test
        @DisplayName("should return failure when validation fails")
        void shouldReturnFailureOnValidationError() {
            UseCaseKey<Integer, Result<Integer>> DOUBLE = UseCaseKey.of("double");

            HexaApp app = HexaFun.dsl()
                .useCase(DOUBLE)
                    .validate(i -> i != null ? Result.ok(i) : Result.fail("Input null"))
                    .handle(i -> Result.ok(i * 2))
                .build();

            Result<Integer> result = app.invoke(DOUBLE, null);
            assertTrue(result.isFailure());
            assertEquals("Input null", result.error());
        }

        @Test
        @DisplayName("should chain multiple validators")
        void shouldChainValidators() {
            UseCaseKey<Integer, Result<Integer>> SAFE_OP = UseCaseKey.of("safeOp");

            HexaApp app = HexaFun.dsl()
                .useCase(SAFE_OP)
                    .validate(i -> i != null ? Result.ok(i) : Result.fail("Input null"))
                    .validate(i -> i >= 0 ? Result.ok(i) : Result.fail("Must be non-negative"))
                    .validate(i -> i <= 100 ? Result.ok(i) : Result.fail("Must be <= 100"))
                    .handle(i -> Result.ok(i * 2))
                .build();

            // All pass
            assertTrue(app.invoke(SAFE_OP, 50).isSuccess());

            // First fails
            assertEquals("Input null", app.invoke(SAFE_OP, (Integer) null).error());

            // Second fails
            assertEquals("Must be non-negative", app.invoke(SAFE_OP, -5).error());

            // Third fails
            assertEquals("Must be <= 100", app.invoke(SAFE_OP, 101).error());
        }

        @Test
        @DisplayName("validators short-circuit on first failure")
        void validatorsShortCircuit() {
            UseCaseKey<Integer, Result<Integer>> KEY = UseCaseKey.of("test");
            int[] callCount = {0};

            HexaApp app = HexaFun.dsl()
                .useCase(KEY)
                    .validate(i -> { callCount[0]++; return Result.fail("First fails"); })
                    .validate(i -> { callCount[0]++; return Result.ok(i); })
                    .handle(i -> Result.ok(i))
                .build();

            app.invoke(KEY, 1);
            assertEquals(1, callCount[0]); // Second validator never called
        }
    }

    @Nested
    @DisplayName("implicit closure")
    class ImplicitClosureTests {

        @Test
        @DisplayName("should register multiple use cases without .and()")
        void shouldRegisterMultipleUseCases() {
            UseCaseKey<Integer, Integer> ADD_ONE = UseCaseKey.of("addOne");
            UseCaseKey<Integer, Integer> ADD_TWO = UseCaseKey.of("addTwo");
            UseCaseKey<Integer, Integer> ADD_THREE = UseCaseKey.of("addThree");

            HexaApp app = HexaFun.dsl()
                .useCase(ADD_ONE).handle(i -> i + 1)
                .useCase(ADD_TWO).handle(i -> i + 2)
                .useCase(ADD_THREE).handle(i -> i + 3)
                .build();

            assertEquals(11, (int) app.invoke(ADD_ONE, 10));
            assertEquals(12, (int) app.invoke(ADD_TWO, 10));
            assertEquals(13, (int) app.invoke(ADD_THREE, 10));
        }

        @Test
        @DisplayName("should track all registered use cases")
        void shouldTrackRegisteredUseCases() {
            UseCaseKey<Object, Object> ONE = UseCaseKey.of("one");
            UseCaseKey<Object, Object> TWO = UseCaseKey.of("two");
            UseCaseKey<Object, Object> THREE = UseCaseKey.of("three");

            HexaApp app = HexaFun.dsl()
                .useCase(ONE).handle(i -> i)
                .useCase(TWO).handle(i -> i)
                .useCase(THREE).handle(i -> i)
                .build();

            var useCases = app.registeredUseCases();
            assertEquals(3, useCases.size());
            assertTrue(useCases.contains("one"));
            assertTrue(useCases.contains("two"));
            assertTrue(useCases.contains("three"));
        }
    }

    @Nested
    @DisplayName("UseCaseKey")
    class UseCaseKeyTests {

        @Test
        @DisplayName("should have correct name")
        void shouldHaveCorrectName() {
            UseCaseKey<String, String> key = UseCaseKey.of("myUseCase");
            assertEquals("myUseCase", key.name());
        }

        @Test
        @DisplayName("should be equal when names match")
        void shouldBeEqualWhenNamesMatch() {
            UseCaseKey<String, String> key1 = UseCaseKey.of("test");
            UseCaseKey<Integer, Integer> key2 = UseCaseKey.of("test");

            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when names differ")
        void shouldNotBeEqualWhenNamesDiffer() {
            UseCaseKey<String, String> key1 = UseCaseKey.of("test1");
            UseCaseKey<String, String> key2 = UseCaseKey.of("test2");

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("should throw on null name")
        void shouldThrowOnNullName() {
            assertThrows(NullPointerException.class, () -> UseCaseKey.of(null));
        }

        @Test
        @DisplayName("toString should include name")
        void toStringShouldIncludeName() {
            UseCaseKey<String, String> key = UseCaseKey.of("myKey");
            assertTrue(key.toString().contains("myKey"));
        }

        @Test
        @DisplayName("should throw when use case not found")
        void shouldThrowWhenNotFound() {
            UseCaseKey<String, String> KEY = UseCaseKey.of("missing");
            HexaApp app = HexaFun.dsl().build();

            assertThrows(IllegalArgumentException.class, () -> app.invoke(KEY, "test"));
        }
    }
}
