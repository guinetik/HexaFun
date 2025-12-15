package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.HexaApp;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Builder for creating HexaApp instances with use cases, ports, and adapters.
 *
 * <p>Supports fluent DSL with implicit closure - each useCase() call
 * automatically closes the previous one.
 *
 * <p>Example:
 * <pre class="language-java">{@code
 * HexaFun.dsl()
 *     .withPort(TaskRepository.class, new InMemoryTaskRepository())
 *     .withAdapter(TO_DTO, task -> new TaskDTO(task.id(), task.title()))
 *     .useCase(Keys.CREATE)
 *         .validate(validator)
 *         .handle(handler)
 *     .useCase(Keys.DELETE)
 *         .handle(deleteHandler)
 *     .build();
 * }</pre>
 */
public class UseCaseBuilder {

    private final Map<String, UseCase<?, ?>> useCases = new HashMap<>();
    private final Map<Class<?>, Object> ports = new HashMap<>();
    private final Map<String, Function<?, ?>> adapters = new HashMap<>();

    // Pending registration - committed on next useCase() or build()
    private String pendingName;
    private UseCase<?, ?> pendingUseCase;

    /**
     * Register a port (output adapter) by its type.
     * Ports are registered when build() is called.
     *
     * <p>Example:
     * <pre class="language-java">{@code
     * HexaFun.dsl()
     *     .withPort(TaskRepository.class, new InMemoryTaskRepository())
     *     .withPort(EmailService.class, new SmtpEmailService())
     *     .useCase(...)
     *     .build();
     * }</pre>
     *
     * @param type The interface/class type to register
     * @param impl The implementation instance
     * @param <T> The port type
     * @return This builder for chaining
     */
    public <T> UseCaseBuilder withPort(Class<T> type, T impl) {
        ports.put(type, impl);
        return this;
    }

    /**
     * Register an adapter with a type-safe key.
     * Adapters transform data from one type to another.
     *
     * <p>Example:
     * <pre class="language-java">{@code
     * HexaFun.dsl()
     *     .withAdapter(TO_INVENTORY, req -> new InventoryCheck(req.itemId()))
     *     .withAdapter(TO_PAYMENT, req -> new PaymentRequest(req.total()))
     *     .useCase(...)
     *     .build();
     * }</pre>
     *
     * @param key The type-safe adapter key
     * @param adapter The transformation function
     * @param <From> The source type
     * @param <To> The target type
     * @return This builder for chaining
     */
    public <From, To> UseCaseBuilder withAdapter(
        AdapterKey<From, To> key,
        Function<From, To> adapter
    ) {
        adapters.put(key.name(), adapter);
        return this;
    }

    /**
     * Start defining a use case with a type-safe key.
     * Implicitly closes any previous use case definition.
     *
     * @param key The type-safe key for this use case
     * @param <I> The input type of the use case
     * @param <O> The output type of the use case
     * @return A new UseCaseInputStep for chaining
     */
    public <I, O> UseCaseInputStep<I, O> useCase(UseCaseKey<I, O> key) {
        commitPending();
        return new UseCaseInputStep<>(key.name(), this);
    }

    /**
     * Stage a use case for registration. Will be committed on next useCase() or build().
     */
    <I, O> void stage(String name, UseCase<I, O> useCase) {
        commitPending();
        this.pendingName = name;
        this.pendingUseCase = useCase;
    }

    /**
     * Commit pending use case to the registry.
     */
    private void commitPending() {
        if (pendingName != null && pendingUseCase != null) {
            useCases.put(pendingName, pendingUseCase);
            pendingName = null;
            pendingUseCase = null;
        }
    }

    /**
     * Build a HexaApp with all the registered use cases and ports.
     * @return A new HexaApp instance
     */
    public HexaApp build() {
        commitPending();

        HexaApp app = HexaApp.create();

        // Register ports
        for (Map.Entry<Class<?>, Object> entry : ports.entrySet()) {
            registerPort(app, entry.getKey(), entry.getValue());
        }

        // Register adapters
        for (Map.Entry<String, Function<?, ?>> entry : adapters.entrySet()) {
            registerAdapter(app, entry.getKey(), entry.getValue());
        }

        // Register use cases
        for (Map.Entry<String, UseCase<?, ?>> entry : useCases.entrySet()) {
            registerUseCase(app, entry.getKey(), entry.getValue());
        }

        return app;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerPort(HexaApp app, Class type, Object impl) {
        app.port(type, impl);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerAdapter(HexaApp app, String name, Function adapter) {
        app.withAdapter(name, adapter);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerUseCase(HexaApp app, String name, UseCase useCase) {
        app.withUseCase(name, useCase);
    }
}
