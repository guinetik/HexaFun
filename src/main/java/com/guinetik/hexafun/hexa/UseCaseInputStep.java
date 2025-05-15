package com.guinetik.hexafun.hexa;

/**
 * Builder step for defining the input handling of a use case.
 * @param <I> The input type of the use case
 */
public class UseCaseInputStep<I> {

    private final String name;
    private final UseCaseBuilder builder;

    public UseCaseInputStep(String name, UseCaseBuilder builder) {
        this.name = name;
        this.builder = builder;
    }

    /**
     * Define the core logic of the use case.
     * @param handler The use case logic that processes the input and produces output
     * @param <O> The output type of the use case
     * @return The next step in the builder chain
     */
    public <O> UseCaseOutputStep<I, O> to(UseCase<I, O> handler) {
        builder.register(name, handler);
        return new UseCaseOutputStep<>(builder);
    }

    /**
     * Define a validation step before the core use case logic.
     * @param validator The validation port that checks the input
     * @return The next step in the builder chain
     */
    public UseCaseValidationStep<I, I> from(ValidationPort<I> validator) {
        return new UseCaseValidationStep<>(name, builder, validator);
    }
}
