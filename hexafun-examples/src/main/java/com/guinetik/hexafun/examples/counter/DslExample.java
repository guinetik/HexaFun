package com.guinetik.hexafun.examples.counter;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;

import com.guinetik.hexafun.examples.counter.CounterOperations.*;

/**
 * The same counter example, but using the DSL for comparison.
 * This shows how much more concise and readable the DSL makes things.
 */
public class DslExample {
    
    public static void main(String[] args) {
        // Create the app using the DSL
        HexaApp app = HexaFun.dsl()
            // Create a use case with the name "increment"
            // The <IncrementInput> is a type hint for the input type of the use case
            // The .from() method is used to validate the input
            // The .to() method is used to define the output of the use case
            // The .and() method is used to chain multiple use cases together
            // The .build() method is used to build the app
            .<IncrementInput>useCase("increment")
                .from(CounterOperations::validateCounter)
                .to(input -> Result.ok(input.getCounter().increment()))
                .and()
            // Create a use case with the name "decrement"
            // The <DecrementInput> is a type hint for the input type of the use case
            // The .from() method is used to validate the input
            // The .to() method is used to define the output of the use case
            // The .and() method is used to chain multiple use cases together
            // The .build() method is used to build the app
            .<DecrementInput>useCase("decrement")
                .from(CounterOperations::validateCounter)
                .to(input -> Result.ok(input.getCounter().decrement()))
                .and()
            // Create a use case with the name "add"
            // The <AddInput> is a type hint for the input type of the use case
            // The .from() method is used to validate the input
            // The .to() method is used to define the output of the use case
            // The .and() method is used to chain multiple use cases together
            // The .build() method is used to build the app
            .<AddInput>useCase("add")
                .from(input -> {
                    Result<AddInput> counterResult = CounterOperations.validateCounter(input);
                    if (counterResult.isFailure()) {
                        return counterResult;
                    }
                    return CounterOperations.validateAmount(input);
                })
                .to(input -> Result.ok(input.getCounter().add(input.getAmount())))
                .and()
            .build();
        
        // Test with some inputs
        Counter initial = Counter.of(10);
        System.out.println("Initial counter: " + initial);
        
        // Increment
        Result<Counter> incrementResult = app.invoke("increment", new IncrementInput(initial));
        
        if (incrementResult.isSuccess()) {
            System.out.println("After increment: " + incrementResult.get());
        } else {
            System.out.println("Increment failed: " + incrementResult.error());
        }
        
        // Decrement
        Result<Counter> decrementResult = app.invoke("decrement", new DecrementInput(initial));
        
        if (decrementResult.isSuccess()) {
            System.out.println("After decrement: " + decrementResult.get());
        } else {
            System.out.println("Decrement failed: " + decrementResult.error());
        }
        
        // Add
        Result<Counter> addResult = app.invoke("add", new AddInput(initial, 5));
        
        if (addResult.isSuccess()) {
            System.out.println("After adding 5: " + addResult.get());
        } else {
            System.out.println("Add failed: " + addResult.error());
        }
        
        // Test validation
        Result<Counter> invalidAddResult = app.invoke("add", new AddInput(initial, 150)); // Outside valid range
        
        if (invalidAddResult.isSuccess()) {
            System.out.println("After adding 150: " + invalidAddResult.get());
        } else {
            System.out.println("Add failed (expected): " + invalidAddResult.error());
        }
    }
}
