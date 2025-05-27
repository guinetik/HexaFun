package com.guinetik.hexafun.hexa;

public class UseCaseOutputStep<I, O> {

    private final UseCaseBuilder builder;

    public UseCaseOutputStep(UseCaseBuilder builder) {
        this.builder = builder;
    }

    public UseCaseBuilder and() {
        return builder;
    }
}
