package com.guinetik.hexafun.examples.manual;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.fun.Result;
import com.guinetik.hexafun.hexa.UseCase;
import com.guinetik.hexafun.hexa.ValidationPort;

import com.guinetik.hexafun.examples.manual.CounterOperations.*;

/**
 * Example of using the raw HexaFun components without the DSL.
 * This shows how to manually create and register use cases.
 */
public class ManualExample {
    
    public static void main(String[] args) {
        // Create our ports (validation and use cases)
        ValidationPort<IncrementInput> incrementValidator = CounterOperations::validateCounter;
        ValidationPort<DecrementInput> decrementValidator = CounterOperations::validateCounter;
        ValidationPort<AddInput> addValidator = input -> {
            Result<AddInput> counterResult = CounterOperations.validateCounter(input);
            if (counterResult.isFailure()) {
                return counterResult;
            }
            return CounterOperations.validateAmount(input);
        };
        
        // Create the use case handlers
        UseCase<IncrementInput, Result<Counter>> incrementHandler = 
                input -> Result.ok(input.getCounter().increment());
        
        UseCase<DecrementInput, Result<Counter>> decrementHandler = 
                input -> Result.ok(input.getCounter().decrement());
        
        UseCase<AddInput, Result<Counter>> addHandler = 
                input -> Result.ok(input.getCounter().add(input.getAmount()));
        
        // Create composite use cases with validation and handling
        UseCase<IncrementInput, Result<Counter>> incrementUseCase = 
                input -> incrementValidator.validate(input).flatMap(i -> incrementHandler.apply(i));
        
        UseCase<DecrementInput, Result<Counter>> decrementUseCase = 
                input -> decrementValidator.validate(input).flatMap(i -> decrementHandler.apply(i));
        
        UseCase<AddInput, Result<Counter>> addUseCase = 
                input -> addValidator.validate(input).flatMap(i -> addHandler.apply(i));
        
        // Create and set up the HexaApp
        HexaApp app = HexaApp.create();
        app.withUseCase("increment", incrementUseCase);
        app.withUseCase("decrement", decrementUseCase);
        app.withUseCase("add", addUseCase);
        
        // Test with some inputs
        Counter initial = Counter.of(10);
        System.out.println("Initial counter: " + initial);
        
        // Increment
        Result<Counter> incrementResult = app.invoke(
                "increment", new IncrementInput(initial));
        
        if (incrementResult.isSuccess()) {
            System.out.println("After increment: " + incrementResult.get());
        } else {
            System.out.println("Increment failed: " + incrementResult.error());
        }
        
        // Decrement
        Result<Counter> decrementResult = app.invoke(
                "decrement", new DecrementInput(initial));
        
        if (decrementResult.isSuccess()) {
            System.out.println("After decrement: " + decrementResult.get());
        } else {
            System.out.println("Decrement failed: " + decrementResult.error());
        }
        
        // Add
        Result<Counter> addResult = app.invoke(
                "add", new AddInput(initial, 5));
        
        if (addResult.isSuccess()) {
            System.out.println("After adding 5: " + addResult.get());
        } else {
            System.out.println("Add failed: " + addResult.error());
        }
        
        // Test validation
        Result<Counter> invalidAddResult = app.invoke(
                "add", new AddInput(initial, 150)); // Outside valid range
        
        if (invalidAddResult.isSuccess()) {
            System.out.println("After adding 150: " + invalidAddResult.get());
        } else {
            System.out.println("Add failed (expected): " + invalidAddResult.error());
        }
    }
}
