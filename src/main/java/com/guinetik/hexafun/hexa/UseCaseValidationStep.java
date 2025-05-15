package com.guinetik.hexafun.hexa;

import java.util.function.Function;

import com.guinetik.hexafun.fun.Result;

public class UseCaseValidationStep<I, M> {

    private final String name;
    private final UseCaseBuilder builder;
    private final Function<I, Result<M>> validator;

    public UseCaseValidationStep(String name, UseCaseBuilder builder, Function<I, Result<M>> validator) {
        this.name = name;
        this.builder = builder;
        this.validator = validator;
    }

    public <O> UseCaseOutputStep<I, Result<O>> to(Function<M, Result<O>> logic) {
        UseCase<I, Result<O>> useCase = input -> validator.apply(input).flatMap(logic);
        builder.register(name, useCase);
        return new UseCaseOutputStep<>(builder);
    }
}
