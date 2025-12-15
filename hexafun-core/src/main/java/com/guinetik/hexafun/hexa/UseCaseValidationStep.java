package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.fun.Result;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder step for defining validation and handler logic.
 *
 * <p>Supports chaining multiple validators that execute in order:
 * <pre class="language-java">{@code
 * .useCase(Keys.ADD)
 *     .validate(Validators::validateCounter)
 *     .validate(Validators::validateAmount)
 *     .handle(input -> ...)
 * }</pre>
 *
 * @param <I> The input type of the use case
 */
public class UseCaseValidationStep<I> {

    private final String name;
    private final UseCaseBuilder builder;
    private final List<ValidationPort<I>> validators;

    public UseCaseValidationStep(
        String name,
        UseCaseBuilder builder,
        ValidationPort<I> validator
    ) {
        this.name = name;
        this.builder = builder;
        this.validators = new ArrayList<>();
        this.validators.add(validator);
    }

    private UseCaseValidationStep(
        String name,
        UseCaseBuilder builder,
        List<ValidationPort<I>> validators
    ) {
        this.name = name;
        this.builder = builder;
        this.validators = validators;
    }

    /**
     * Add another validator to the chain.
     * Validators execute in order; first failure short-circuits.
     *
     * @param validator The next validation port
     * @return This step for further chaining
     */
    public UseCaseValidationStep<I> validate(ValidationPort<I> validator) {
        List<ValidationPort<I>> newValidators = new ArrayList<>(
            this.validators
        );
        newValidators.add(validator);
        return new UseCaseValidationStep<>(name, builder, newValidators);
    }

    /**
     * Define the handler that runs after all validators pass.
     *
     * @param handler The use case logic that processes the validated input
     * @param <O> The output type of the handler
     * @return The builder for chaining more use cases
     */
    public <O> UseCaseBuilder handle(UseCase<I, Result<O>> handler) {
        ValidationPort<I> composedValidator = composeValidators();

        UseCase<I, Result<O>> useCase = input -> {
            Result<I> validationResult = composedValidator.validate(input);
            if (validationResult.isFailure()) {
                return Result.fail(validationResult.error());
            }
            return handler.apply(validationResult.get());
        };

        builder.stage(name, useCase);
        return builder;
    }

    private ValidationPort<I> composeValidators() {
        return input -> {
            Result<I> result = Result.ok(input);
            for (ValidationPort<I> validator : validators) {
                result = result.flatMap(validator::validate);
                if (result.isFailure()) {
                    return result;
                }
            }
            return result;
        };
    }
}
