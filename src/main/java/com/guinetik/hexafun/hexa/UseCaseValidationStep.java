package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.fun.Result;

/**
 * Builder step for defining the validation and then use case logic.
 * @param <I> The input type of the use case
 * @param <V> The validated type (usually same as I)
 */
public class UseCaseValidationStep<I, V> {

    private final String name;
    private final UseCaseBuilder builder;
    private final ValidationPort<I> validator;

    public UseCaseValidationStep(String name, UseCaseBuilder builder, ValidationPort<I> validator) {
        this.name = name;
        this.builder = builder;
        this.validator = validator;
    }

    /**
     * Define the core logic of the use case that will run after validation.
     * @param handler The use case logic that processes the validated input
     * @param <O> The output type of the use case
     * @return The next step in the builder chain
     */
    public <O> UseCaseOutputStep<I, Result<O>> to(UseCase<I, Result<O>> handler) {
        // Create a composite use case that validates and then processes
        UseCase<I, Result<O>> useCase = input -> {
            Result<I> validationResult = validator.validate(input);
            if (validationResult.isFailure()) {
                return Result.fail(validationResult.error());
            }
            return handler.apply(validationResult.get());
        };
        
        builder.register(name, useCase);
        return new UseCaseOutputStep<>(builder);
    }
}
