package com.guinetik.hexafun.hexa;

@FunctionalInterface
public interface OutputPort<I, O> {
    O apply(I input);
}