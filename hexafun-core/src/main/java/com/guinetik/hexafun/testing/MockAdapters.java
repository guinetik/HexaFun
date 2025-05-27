package com.guinetik.hexafun.testing;

import com.guinetik.hexafun.hexa.InputPort;
import com.guinetik.hexafun.hexa.OutputPort;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility for creating mock adapters for testing.
 */
public class MockAdapters {
    
    /**
     * Create a mock InputPort that returns a fixed value.
     * @param value The value to return
     * @param <I> Input type
     * @param <O> Output type
     * @return An InputPort that ignores its input and returns the fixed value
     */
    public static <I, O> InputPort<I, O> inputWith(O value) {
        return input -> value;
    }
    
    /**
     * Create a mock InputPort using the provided function.
     * @param function The function to process inputs
     * @param <I> Input type
     * @param <O> Output type
     * @return An InputPort that uses the function
     */
    public static <I, O> InputPort<I, O> input(Function<I, O> function) {
        return function::apply;
    }
    
    /**
     * Create a mock OutputPort using the provided function.
     * @param function The function to process inputs
     * @param <I> Input type
     * @param <O> Output type
     * @return An OutputPort that uses the function
     */
    public static <I, O> OutputPort<I, O> output(Function<I, O> function) {
        return function::apply;
    }
    
    /**
     * Create a repository-like mock that stores values by key.
     * @param <K> Key type
     * @param <V> Value type
     * @return A simple in-memory repository
     */
    public static <K, V> InMemoryRepository<K, V> repository() {
        return new InMemoryRepository<>();
    }
    
    /**
     * A simple in-memory repository for testing.
     * @param <K> Key type
     * @param <V> Value type
     */
    public static class InMemoryRepository<K, V> {
        private final Map<K, V> store = new HashMap<>();
        
        public V get(K key) {
            return store.get(key);
        }
        
        public void put(K key, V value) {
            store.put(key, value);
        }
        
        public boolean contains(K key) {
            return store.containsKey(key);
        }
        
        public void remove(K key) {
            store.remove(key);
        }
        
        public Map<K, V> getAll() {
            return new HashMap<>(store);
        }
        
        public void clear() {
            store.clear();
        }
    }
}
