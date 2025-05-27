package com.guinetik.hexafun;

import com.guinetik.hexafun.hexa.UseCaseBuilder;

public final class HexaFun {
    private HexaFun() {}

    public static UseCaseBuilder dsl() {
        return new UseCaseBuilder();
    }
}