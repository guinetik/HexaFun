package com.guinetik.hexafun;

import com.guinetik.hexafun.hexa.AdapterKey;
import com.guinetik.hexafun.hexa.UseCase;
import com.guinetik.hexafun.hexa.UseCaseKey;
import com.guinetik.hexafun.testing.HexaTest;
import com.guinetik.hexafun.testing.UseCaseTest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Core container for a Hexagonal Architecture application.
 * Manages use cases, ports, and adapters.
 */
public abstract class HexaApp {

    protected final Map<String, UseCase<?, ?>> useCases = new HashMap<>();
    protected final Map<Class<?>, Object> ports = new HashMap<>();
    protected final Map<String, Function<?, ?>> adapters = new HashMap<>();

    /**
     * Create a new empty HexaApp.
     * @return A new empty HexaApp
     */
    public static HexaApp create() {
        return new HexaAppImpl();
    }

    /**
     * Add a use case to this HexaApp (used internally by builder).
     */
    public <I, O> HexaApp withUseCase(String name, UseCase<I, O> useCase) {
        useCases.put(name, useCase);
        return this;
    }

    /**
     * Register a port (output adapter) by its type.
     * Provides type-safe dependency injection for output ports.
     *
     * <p>Example:
     * <pre class="language-java">{@code
     * app.port(TaskRepository.class, new InMemoryTaskRepository());
     * }</pre>
     *
     * @param type The interface/class type to register
     * @param impl The implementation instance
     * @param <T> The port type
     * @return This HexaApp for chaining
     */
    public <T> HexaApp port(Class<T> type, T impl) {
        ports.put(type, impl);
        return this;
    }

    /**
     * Retrieve a port by its type.
     *
     * <p>Example:
     * <pre class="language-java">{@code
     * TaskRepository repo = app.port(TaskRepository.class);
     * }</pre>
     *
     * @param type The interface/class type to retrieve
     * @param <T> The port type
     * @return The registered implementation
     * @throws IllegalArgumentException if no port is registered for the given type
     */
    @SuppressWarnings("unchecked")
    public <T> T port(Class<T> type) {
        T impl = (T) ports.get(type);
        if (impl == null) {
            throw new IllegalArgumentException(
                "No port registered for type: " + type.getName()
            );
        }
        return impl;
    }

    /**
     * Check if a port is registered for the given type.
     *
     * @param type The interface/class type to check
     * @return true if a port is registered, false otherwise
     */
    public boolean hasPort(Class<?> type) {
        return ports.containsKey(type);
    }

    /**
     * Get all registered port types.
     *
     * @return A set of registered port types
     */
    public Set<Class<?>> registeredPorts() {
        return ports.keySet();
    }

    // ===== Adapters =====

    /**
     * Register an adapter with a type-safe key.
     * Adapters transform data from one type to another.
     *
     * <p>Example:
     * <pre class="language-java">{@code
     * app.withAdapter(TO_INVENTORY, req -> new InventoryCheck(req.itemId()));
     * }</pre>
     *
     * @param key The type-safe adapter key
     * @param adapter The adapter function
     * @param <From> The source type
     * @param <To> The target type
     * @return This HexaApp for chaining
     */
    public <From, To> HexaApp withAdapter(AdapterKey<From, To> key, Function<From, To> adapter) {
        adapters.put(key.name(), adapter);
        return this;
    }

    /**
     * Register an adapter by name (used internally by builder).
     *
     * @param name The adapter name
     * @param adapter The adapter function
     * @param <From> The source type
     * @param <To> The target type
     * @return This HexaApp for chaining
     */
    public <From, To> HexaApp withAdapter(String name, Function<From, To> adapter) {
        adapters.put(name, adapter);
        return this;
    }

    /**
     * Adapt a value using a type-safe adapter key.
     * Transforms the input from one type to another.
     *
     * <p>Example:
     * <pre class="language-java">{@code
     * InventoryCheck check = app.adapt(TO_INVENTORY, orderRequest);
     * }</pre>
     *
     * @param key The type-safe adapter key
     * @param input The value to adapt
     * @param <From> The source type
     * @param <To> The target type
     * @return The adapted value
     * @throws IllegalArgumentException if no adapter is registered with the given key
     */
    @SuppressWarnings("unchecked")
    public <From, To> To adapt(AdapterKey<From, To> key, From input) {
        Function<From, To> adapter = (Function<From, To>) adapters.get(key.name());
        if (adapter == null) {
            throw new IllegalArgumentException(
                "No adapter registered with name: " + key.name()
            );
        }
        return adapter.apply(input);
    }

    /**
     * Check if an adapter is registered for the given key.
     *
     * @param key The adapter key to check
     * @return true if an adapter is registered, false otherwise
     */
    public boolean hasAdapter(AdapterKey<?, ?> key) {
        return adapters.containsKey(key.name());
    }

    /**
     * Get all registered adapter names.
     *
     * @return A set of registered adapter names
     */
    public Set<String> registeredAdapters() {
        return adapters.keySet();
    }

    /**
     * Invoke a use case using a type-safe key.
     * Provides compile-time type checking for input and output types.
     *
     * @param key The type-safe key for the use case
     * @param input The input to the use case
     * @param <I> The input type of the use case
     * @param <O> The output type of the use case
     * @return The result of the use case
     * @throws IllegalArgumentException if no use case is registered with the given key
     */
    @SuppressWarnings("unchecked")
    public <I, O> O invoke(UseCaseKey<I, O> key, I input) {
        UseCase<I, O> useCase = (UseCase<I, O>) useCases.get(key.name());
        if (useCase == null) {
            throw new IllegalArgumentException(
                "No use case registered with name: " + key.name()
            );
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
     * Invoke a use case by name (for internal/testing use).
     * Prefer using {@link #invoke(UseCaseKey, Object)} for type safety.
     *
     * @param name The name of the use case
     * @param input The input to the use case
     * @param <I> The input type
     * @param <O> The output type
     * @return The result of the use case
     * @throws IllegalArgumentException if no use case is registered with the given name
     */
    @SuppressWarnings("unchecked")
    public <I, O> O invokeByName(String name, I input) {
        UseCase<I, O> useCase = (UseCase<I, O>) useCases.get(name);
        if (useCase == null) {
            throw new IllegalArgumentException(
                "No use case registered with name: " + name
            );
        }
        return useCase.apply(input);
    }

    /**
     * Start testing a use case using a type-safe key.
     *
     * @param key The type-safe key for the use case
     * @param <I> Input type of the use case
     * @param <O> Output type of the use case
     * @return A new UseCaseTest instance
     */
    public <I, O> UseCaseTest<I, O> test(UseCaseKey<I, O> key) {
        return HexaTest.forApp(this).test(key.name());
    }

    /**
     * Optional startup logic.
     */
    public void run() {
        // optional startup logic
    }
}
