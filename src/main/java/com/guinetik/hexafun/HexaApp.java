package com.guinetik.hexafun;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.guinetik.hexafun.hexa.InputPort;
import com.guinetik.hexafun.hexa.OutputPort;
import com.guinetik.hexafun.hexa.UseCase;
import com.guinetik.hexafun.testing.HexaTest;
import com.guinetik.hexafun.testing.UseCaseTest;

/**
 * Core container for a Hexagonal Architecture application.
 * Manages use cases and adapters.
 */
public abstract class HexaApp {

    protected final Map<String, UseCase<?, ?>> useCases = new HashMap<>();
    protected final Map<String, InputPort<?, ?>> inputAdapters = new HashMap<>();
    protected final Map<String, OutputPort<?, ?>> outputAdapters = new HashMap<>();

    /**
     * Create a new empty HexaApp.
     * @return A new empty HexaApp
     */
    public static HexaApp create() {
        return new HexaAppImpl();
    }

    /**
     * Add a use case to this HexaApp.
     * @param name The name of the use case
     * @param useCase The use case implementation
     * @param <I> The input type of the use case
     * @param <O> The output type of the use case
     * @return This HexaApp for chaining
     */
    public <I, O> HexaApp withUseCase(String name, UseCase<I, O> useCase) {
        useCases.put(name, useCase);
        return this;
    }

    /**
     * Add an input adapter to this HexaApp.
     * @param name The name of the adapter
     * @param adapter The adapter implementation
     * @param <I> The input type of the adapter
     * @param <O> The output type of the adapter
     * @return This HexaApp for chaining
     */
    public <I, O> HexaApp withAdapter(String name, InputPort<I, O> adapter) {
        inputAdapters.put(name, adapter);
        return this;
    }

    /**
     * Add an output adapter to this HexaApp.
     * @param name The name of the adapter
     * @param adapter The adapter implementation
     * @param <I> The input type of the adapter
     * @param <O> The output type of the adapter
     * @return This HexaApp for chaining
     */
    public <I, O> HexaApp withAdapter(String name, OutputPort<I, O> adapter) {
        outputAdapters.put(name, adapter);
        return this;
    }

    /**
     * Invoke a use case by name.
     * @param name The name of the use case to invoke
     * @param input The input to the use case
     * @param <I> The input type of the use case
     * @param <O> The output type of the use case
     * @return The result of the use case
     * @throws IllegalArgumentException if no use case is registered with the given name
     */
    @SuppressWarnings("unchecked")
    public <I, O> O invoke(String name, I input) {
        UseCase<I, O> useCase = (UseCase<I, O>) useCases.get(name);
        if (useCase == null) {
            throw new IllegalArgumentException("No use case registered with name: " + name);
        }
        return useCase.apply(input);
    }

    /**
     * Get the names of all registered use cases.
     * @return A set of registered use case names
     */
    public Set<String> registeredUseCases() {
        return useCases.keySet();
    }
    
    /**
     * Start testing a use case in this app.
     * @param useCaseName Name of the use case to test
     * @param <I> Input type of the use case
     * @param <O> Output type of the use case
     * @return A new UseCaseTest instance
     */
    public <I, O> UseCaseTest<I, O> test(String useCaseName) {
        return HexaTest.forApp(this).test(useCaseName);
    }

    /**
     * Optional startup logic.
     */
    public void run() {
        // optional startup logic
    }
}
