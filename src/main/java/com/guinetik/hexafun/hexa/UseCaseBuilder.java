package com.guinetik.hexafun.hexa;

import java.util.HashMap;
import java.util.Map;

import com.guinetik.hexafun.HexaApp;

/**
 * Builder for creating HexaApp instances with use cases.
 */
public class UseCaseBuilder {

    private final Map<String, UseCase<?, ?>> useCases = new HashMap<>();

    /**
     * Start defining a use case.
     * @param name The name of the use case
     * @param <I> The input type of the use case
     * @return A new UseCaseInputStep for chaining
     */
    public <I> UseCaseInputStep<I> useCase(String name) {
        return new UseCaseInputStep<>(name, this);
    }

    /**
     * Register a use case with this builder.
     * @param name The name of the use case
     * @param useCase The use case implementation
     * @param <I> The input type of the use case
     * @param <O> The output type of the use case
     */
    <I, O> void register(String name, UseCase<I, O> useCase) {
        useCases.put(name, useCase);
    }

    /**
     * Build a HexaApp with all the registered use cases.
     * @return A new HexaApp instance
     */
    public HexaApp build() {
        // Create a new HexaApp instance and register all use cases
        HexaApp app = HexaApp.create();
        for (Map.Entry<String, UseCase<?, ?>> entry : useCases.entrySet()) {
            registerUseCase(app, entry.getKey(), entry.getValue());
        }
        return app;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerUseCase(HexaApp app, String name, UseCase useCase) {
        app.withUseCase(name, useCase);
    }
}
