package com.guinetik.hexafun.hexa;

@FunctionalInterface
public interface UseCase<I, O> {
    O apply(I input);
}