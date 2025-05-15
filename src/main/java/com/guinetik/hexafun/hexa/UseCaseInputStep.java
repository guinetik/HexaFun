package com.guinetik.hexafun.hexa;

import java.util.function.Function;

import com.guinetik.hexafun.fun.Result;

public class UseCaseInputStep<I> {

    private final String name;
    private final UseCaseBuilder builder;

    public UseCaseInputStep(String name, UseCaseBuilder builder) {
        this.name = name;
        this.builder = builder;
    }

    public <O> UseCaseOutputStep<I, O> to(Function<I, O> logic) {
        UseCase<I, O> uc = logic::apply;
        builder.register(name, uc);
        return new UseCaseOutputStep<>(builder);
    }

    public <M> UseCaseValidationStep<I, M> from(Function<I, Result<M>> validator) {
        return new UseCaseValidationStep<>(name, builder, validator);
    }
}