package com.guinetik.hexafun.hexa;

/**
 * Builder step for defining the input handling of a use case.
 *
 * <p>From here you can:
 * <ul>
 *   <li>{@link #validate(ValidationPort)} - Add validation before handling</li>
 *   <li>{@link #handle(UseCase)} - Go directly to handler (no validation)</li>
 * </ul>
 *
 * @param <I> The input type of the use case
 * @param <O> The output type of the use case
 */
public class UseCaseInputStep<I, O> {

    private final String name;
    private final UseCaseBuilder builder;

    public UseCaseInputStep(String name, UseCaseBuilder builder) {
        this.name = name;
        this.builder = builder;
    }

    /**
     * Define the core logic of the use case without validation.
     *
     * @param handler The use case logic that processes the input and produces output
     * @param <R> The output type of the use case
     * @return The builder for chaining more use cases
     */
    public <R> UseCaseBuilder handle(UseCase<I, R> handler) {
        builder.stage(name, handler);
        return builder;
    }

    /**
     * Add a validation step before the handler.
     *
     * @param validator The validation port that checks the input
     * @return The validation step for chaining more validators or the handler
     */
    public UseCaseValidationStep<I> validate(ValidationPort<I> validator) {
        return new UseCaseValidationStep<>(name, builder, validator);
    }
}
