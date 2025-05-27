package com.guinetik.hexafun.examples.counter;

import com.guinetik.hexafun.fun.Result;

/**
 * Simple input and output classes for our counter operations.
 */
public class CounterOperations {
    
    /**
     * Input for increment operation.
     */
    public static class IncrementInput {
        private final Counter counter;
        
        public IncrementInput(Counter counter) {
            this.counter = counter;
        }
        
        public Counter getCounter() {
            return counter;
        }
    }
    
    /**
     * Input for decrement operation.
     */
    public static class DecrementInput {
        private final Counter counter;
        
        public DecrementInput(Counter counter) {
            this.counter = counter;
        }
        
        public Counter getCounter() {
            return counter;
        }
    }
    
    /**
     * Input for add operation.
     */
    public static class AddInput {
        private final Counter counter;
        private final int amount;
        
        public AddInput(Counter counter, int amount) {
            this.counter = counter;
            this.amount = amount;
        }
        
        public Counter getCounter() {
            return counter;
        }
        
        public int getAmount() {
            return amount;
        }
    }
    
    /**
     * Validates that a counter is not null.
     */
    public static <T> Result<T> validateCounter(T input) {
        if (input == null) {
            return Result.fail("Input cannot be null");
        }
        
        Counter counter = null;
        
        if (input instanceof IncrementInput) {
            counter = ((IncrementInput) input).getCounter();
        } else if (input instanceof DecrementInput) {
            counter = ((DecrementInput) input).getCounter();
        } else if (input instanceof AddInput) {
            counter = ((AddInput) input).getCounter();
        }
        
        if (counter == null) {
            return Result.fail("Counter cannot be null");
        }
        
        return Result.ok(input);
    }
    
    /**
     * Validates that an amount is within range.
     */
    public static Result<AddInput> validateAmount(AddInput input) {
        if (input.getAmount() < -100 || input.getAmount() > 100) {
            return Result.fail("Amount must be between -100 and 100");
        }
        
        return Result.ok(input);
    }
}
