package com.guinetik.hexafun.examples.todo;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;
import com.guinetik.hexafun.hexa.UseCase;
import com.guinetik.hexafun.hexa.ValidationPort;

import java.util.List;
import java.util.Set;

/**
 * Example TodoApp built with HexaFun.
 * Demonstrates a simple Hexagonal Architecture application.
 */
public class TodoApp {
    
    private final TodoRepository repository;
    private final HexaApp app;
    
    /**
     * Create a new TodoApp with the given repository.
     * @param repository The TodoRepository to use
     */
    public TodoApp(TodoRepository repository) {
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
        return app.<CompleteTodoInput, Result<Todo>>invoke("completeTodo", input);
    }
    
    /**
     * List all Todos.
     * @return Result containing the list of Todos
     */
    public Result<List<Todo>> listTodos() {
        return app.<Object, Result<List<Todo>>>invoke("listTodos", null);
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
            if (input.getTitle() == null || input.getTitle().isBlank()) {
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
            if (input.getId() == null || input.getId().isBlank()) {
                return Result.fail("Todo ID cannot be empty");
            }
            return Result.ok(input);
        };
    }
    
    // Use case ports
    private UseCase<CreateTodoInput, Result<Todo>> createTodoHandler() {
        return input -> {
            Todo todo = Todo.builder()
                    .title(input.getTitle())
                    .description(input.getDescription())
                    .build();
            return Result.ok(repository.save(todo));
        };
    }
    
    private UseCase<CompleteTodoInput, Result<Todo>> completeTodoHandler() {
        return input -> repository.findById(input.getId())
                .map(Todo::markComplete)
                .map(repository::save)
                .map(Result::ok)
                .orElse(Result.fail("Todo not found with ID: " + input.getId()));
    }
    
    private UseCase<Object, Result<List<Todo>>> listTodosHandler() {
        return unused -> Result.ok(repository.findAll());
    }
    
    /**
     * Create a new TodoApp with an in-memory repository.
     * @return A new TodoApp
     */
    public static TodoApp createInMemory() {
        return new TodoApp(new InMemoryTodoRepository());
    }
}
