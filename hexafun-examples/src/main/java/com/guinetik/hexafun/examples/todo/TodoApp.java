package com.guinetik.hexafun.examples.todo;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;
import com.guinetik.hexafun.hexa.UseCase;
import com.guinetik.hexafun.hexa.ValidationPort;
import com.guinetik.hexafun.hexa.HexaRepo;
import com.guinetik.hexafun.hexa.InMemoryHexaRepo;

import java.util.List;
import java.util.Set;

/**
 * Example TodoApp built with HexaFun.
 * Demonstrates a simple Hexagonal Architecture application.
 */
public class TodoApp {
    
    private final HexaRepo<Todo> repository;
    private final HexaApp app;
    
    /**
     * Create a new TodoApp with the given repository.
     * @param repository The TodoRepository to use
     */
    public TodoApp(HexaRepo<Todo> repository) {
        this.repository = repository;
        
        // Build the application with use cases
        this.app = HexaFun.dsl()
            .<CreateTodoInput>useCase("createTodo")
                .from(createTodoValidator())
                .to(createTodoHandler())
                .and()
            .<CompleteTodoInput>useCase("completeTodo")
                .from(completeTodoValidator())
                .to(completeTodoHandler())
                .and()
            .<Object>useCase("listTodos")
                .to(listTodosHandler())
                .and()
            .build();
    }
    
    /**
     * Get the underlying HexaApp instance.
     * @return The HexaApp
     */
    public HexaApp getApp() {
        return app;
    }
    
    /**
     * Create a new Todo.
     * @param input The CreateTodoInput
     * @return Result containing the created Todo, or an error
     */
    public Result<Todo> createTodo(CreateTodoInput input) {
        return app.<CreateTodoInput, Result<Todo>>invoke("createTodo", input);
    }
    
    /**
     * Complete a Todo.
     * @param input The CompleteTodoInput
     * @return Result containing the completed Todo, or an error
     */
    public Result<Todo> completeTodo(CompleteTodoInput input) {
        return app.invoke("completeTodo", input);
    }
    
    /**
     * List all Todos.
     * @return Result containing the list of Todos
     */
    public Result<List<Todo>> listTodos() {
        return app.invoke("listTodos", null);
    }
    
    /**
     * Debug method to print registered use cases.
     */
    public void printRegisteredUseCases() {
        System.out.println("\nRegistered Use Cases in TodoApp:");
        Set<String> cases = app.registeredUseCases();
        if (cases.isEmpty()) {
            System.out.println("No use cases registered!");
        } else {
            cases.forEach(name -> System.out.println(" - " + name));
        }
    }
    
    // ------------- PORT IMPLEMENTATIONS -------------
    
    // Validation ports
    private ValidationPort<CreateTodoInput> createTodoValidator() {
        return input -> {
            if (input == null) {
                return Result.fail("Input cannot be null");
            }
            if (input.title() == null || input.title().isBlank()) {
                return Result.fail("Title cannot be empty");
            }
            return Result.ok(input);
        };
    }
    
    private ValidationPort<CompleteTodoInput> completeTodoValidator() {
        return input -> {
            if (input == null) {
                return Result.fail("Input cannot be null");
            }
            if (input.id() == null || input.id().isBlank()) {
                return Result.fail("Todo ID cannot be empty");
            }
            return Result.ok(input);
        };
    }
    
    // Use case ports
    private UseCase<CreateTodoInput, Result<Todo>> createTodoHandler() {
        return input -> {
            Todo todo = Todo.builder()
                    .title(input.title())
                    .description(input.description())
                    .build();
            return repository.save(todo);
        };
    }
    
    private UseCase<CompleteTodoInput, Result<Todo>> completeTodoHandler() {
        return input -> repository.findById(input.id())
                .flatMap(todo -> repository.save(todo.markComplete()));
    }
    
    private UseCase<Object, Result<List<Todo>>> listTodosHandler() {
        return unused -> repository.findAll();
    }
    
    /**
     * Create a new TodoApp with an in-memory repository.
     * @return A new TodoApp
     */
    public static TodoApp createInMemory() {
        return new TodoApp(new InMemoryHexaRepo<Todo>());
    }
}
