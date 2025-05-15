package com.guinetik.hexafun.examples.manual;

/**
 * A simple counter domain model.
 * Immutable value object with increment/decrement operations.
 */
public class Counter {
    private final int value;
    
    private Counter(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public Counter increment() {
        return new Counter(value + 1);
    }
    
    public Counter decrement() {
        return new Counter(value - 1);
    }
    
    public Counter add(int amount) {
        return new Counter(value + amount);
    }
    
    public static Counter of(int initialValue) {
        return new Counter(initialValue);
    }
    
    @Override
    public String toString() {
        return "Counter(" + value + ")";
    }
}
