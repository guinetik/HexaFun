package com.guinetik.hexafun.fun;

import java.util.Objects;
import java.util.function.Function;

public sealed interface Result<T> permits Result.Success, Result.Failure {

    static <T> Result<T> ok(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> fail(String message) {
        return new Failure<>(message);
    }

    boolean isSuccess();
    boolean isFailure();

    T get(); // orElseThrow?
    String error();

    <U> Result<U> map(Function<T, U> mapper);
    <U> Result<U> flatMap(Function<T, Result<U>> mapper);

    <U> U fold(Function<String, U> onFailure, Function<T, U> onSuccess);

    final class Success<T> implements Result<T> {
        private final T value;

        public Success(T value) {
            this.value = Objects.requireNonNull(value);
        }

        public boolean isSuccess() { return true; }
        public boolean isFailure() { return false; }
        public T get() { return value; }
        public String error() { throw new IllegalStateException("No error"); }

        public <U> Result<U> map(Function<T, U> mapper) {
            return Result.ok(mapper.apply(value));
        }

        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            return mapper.apply(value);
        }

        public <U> U fold(Function<String, U> onFailure, Function<T, U> onSuccess) {
            return onSuccess.apply(value);
        }
    }

    final class Failure<T> implements Result<T> {
        private final String error;

        public Failure(String error) {
            this.error = Objects.requireNonNull(error);
        }

        public boolean isSuccess() { return false; }
        public boolean isFailure() { return true; }
        public T get() { throw new IllegalStateException(error); }
        public String error() { return error; }

        public <U> Result<U> map(Function<T, U> mapper) {
            return Result.fail(error);
        }

        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            return Result.fail(error);
        }

        public <U> U fold(Function<String, U> onFailure, Function<T, U> onSuccess) {
            return onFailure.apply(error);
        }
    }
}