package com.guinetik.hexafun.examples.counter;

/**
 * Immutable counter value object.
 */
public record Counter(int value) {
    public Counter increment() {
        return new Counter(value + 1);
    }

    public Counter decrement() {
        return new Counter(value - 1);
    }

    public Counter add(int amount) {
        return new Counter(value + amount);
    }

    public static Counter zero() {
        return new Counter(0);
    }

    public static Counter of(int value) {
        return new Counter(value);
    }
}
