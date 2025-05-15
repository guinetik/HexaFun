package com.guinetik.hexafun.hexa;

@FunctionalInterface
public interface InputPort<I, O> {
    O handle(I input);
}