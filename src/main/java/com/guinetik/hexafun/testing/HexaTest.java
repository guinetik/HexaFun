package com.guinetik.hexafun.testing;

import com.guinetik.hexafun.HexaApp;

/**
 * Entry point for the HexaFun testing DSL.
 * Provides a fluent API for testing use cases in a HexaApp.
 */
public class HexaTest {
    private final HexaApp app;

    private HexaTest(HexaApp app) {
        this.app = app;
    }

    /**
     * Create a new HexaTest for the given app.
     * @param app The HexaApp to test
     * @return A new HexaTest instance
     */
    public static HexaTest forApp(HexaApp app) {
        return new HexaTest(app);
    }

    /**
     * Start testing a use case.
     * @param useCaseName Name of the use case to test
     * @param <I> Input type of the use case
     * @param <O> Output type of the use case
     * @return A new UseCaseTest instance
     */
    public <I, O> UseCaseTest<I, O> useCase(String useCaseName) {
        return new UseCaseTest<>(app, useCaseName);
    }
    
    /**
     * Start a test for the specified use case.
     * Simply a shorter method name for useCase() to better match the example in the README.
     * @param useCaseName Name of the use case to test
     * @param <I> Input type of the use case
     * @param <O> Output type of the use case
     * @return A new UseCaseTest instance
     */
    public <I, O> UseCaseTest<I, O> test(String useCaseName) {
        return useCase(useCaseName);
    }

    /**
     * Verify that a use case exists in the app.
     * @param useCaseName Name of the use case to check
     * @return This HexaTest instance
     * @throws AssertionError if the use case doesn't exist
     */
    public HexaTest hasUseCase(String useCaseName) {
        if (!app.registeredUseCases().contains(useCaseName)) {
            throw new AssertionError("Expected use case '" + useCaseName + "' to exist, but it doesn't");
        }
        return this;
    }

    /**
     * Verify that a use case does not exist in the app.
     * @param useCaseName Name of the use case to check
     * @return This HexaTest instance
     * @throws AssertionError if the use case exists
     */
    public HexaTest doesNotHaveUseCase(String useCaseName) {
        if (app.registeredUseCases().contains(useCaseName)) {
            throw new AssertionError("Expected use case '" + useCaseName + "' to not exist, but it does");
        }
        return this;
    }
}
