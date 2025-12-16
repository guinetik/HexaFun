package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.HexaApp;

/**
 * Abstract base class for use case handlers that need access to ports.
 *
 * <p>This class provides a convenient way to implement use cases that require
 * infrastructure dependencies (ports) while maintaining the functional interface
 * contract of {@link UseCase}.</p>
 *
 * <h2>When to use UseCaseHandler</h2>
 * <ul>
 *   <li>When your use case needs to access repositories, external services, or other ports</li>
 *   <li>When you want to externalize handler logic into dedicated classes</li>
 *   <li>When handlers need to be tested with different port implementations</li>
 * </ul>
 *
 * <h2>When to use lambdas instead</h2>
 * <ul>
 *   <li>For simple, pure transformations with no port dependencies</li>
 *   <li>For one-liner handlers that don't need testing in isolation</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre class="language-java">{@code
 * public class CreateTaskHandler extends UseCaseHandler<CreateTask, Result<Task>> {
 *     public CreateTaskHandler(HexaApp app) {
 *         super(app);
 *     }
 *
 *     @Override
 *     public Result<Task> apply(CreateTask input) {
 *         TaskRepository repo = port(TaskRepository.class);
 *         Task task = Task.create(input.title());
 *         return Result.ok(repo.save(task));
 *     }
 * }
 *
 * // Registration
 * HexaApp app = HexaFun.dsl()
 *     .port(TaskRepository.class, new InMemoryTaskRepo())
 *     .useCase(CREATE_TASK)
 *         .handle(new CreateTaskHandler(app))
 *     .build();
 * }</pre>
 *
 * @param <I> The input type for this use case
 * @param <O> The output type for this use case
 * @see UseCase
 * @see HexaApp#port(Class)
 */
public abstract class UseCaseHandler<I, O> implements UseCase<I, O> {

    /**
     * Reference to the HexaApp for port access.
     */
    protected final HexaApp app;

    /**
     * Creates a new handler with access to the given HexaApp.
     *
     * @param app The HexaApp instance for port access
     */
    protected UseCaseHandler(HexaApp app) {
        this.app = app;
    }

    /**
     * Convenience method to retrieve a port by its type.
     *
     * <p>This is a shorthand for {@code app.port(type)}.</p>
     *
     * @param type The interface/class type to retrieve
     * @param <T> The port type
     * @return The registered implementation
     * @throws IllegalArgumentException if no port is registered for the given type
     */
    protected <T> T port(Class<T> type) {
        return app.port(type);
    }

    /**
     * Check if a port is registered for the given type.
     *
     * @param type The interface/class type to check
     * @return true if a port is registered, false otherwise
     */
    protected boolean hasPort(Class<?> type) {
        return app.hasPort(type);
    }
}
