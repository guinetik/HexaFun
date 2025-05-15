package com.guinetik.hexafun.examples.todo;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;

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
     * 
     * @param repository The TodoRepository to use
     */
    public TodoApp(TodoRepository repository) {
        this.repository = repository;
        this.app = HexaFun.dsl()
                .<CreateTodoInput>useCase("createTodo")
                    .from(input -> validateCreateTodo(input))
                    .to(input -> createTodoHandler(input))
                .and()
                .<CompleteTodoInput>useCase("completeTodo")
                    .from(input -> validateCompleteTodo(input))
                    .to(input -> completeTodoHandler(input))
                .and()
                .useCase("listTodos")
                    .to(unused -> listTodosHandler(unused))
                .and()
                .build();
    }

    /**
     * Get the underlying HexaApp instance.
     * 
     * @return The HexaApp
     */
    public HexaApp getApp() {
        return app;
    }

    /**
     * Create a new Todo.
     * 
     * @param input The CreateTodoInput
     * @return Result containing the created Todo, or an error
     */
    public Result<Todo> createTodo(CreateTodoInput input) {
        return app.invoke("createTodo", input);
    }

    /**
     * Complete a Todo.
     * 
     * @param input The CompleteTodoInput
     * @return Result containing the completed Todo, or an error
     */
    public Result<Todo> completeTodo(CompleteTodoInput input) {
        return app.invoke("completeTodo", input);
    }

    /**
     * List all Todos.
     * 
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

    // ------------- USE CASE HANDLERS -------------

    // Validation functions
    private Result<CreateTodoInput> validateCreateTodo(CreateTodoInput input) {
        if (input == null) {
            return Result.fail("Input cannot be null");
        }
        if (input.getTitle() == null || input.getTitle().isBlank()) {
            return Result.fail("Title cannot be empty");
        }
        return Result.ok(input);
    }

    private Result<CompleteTodoInput> validateCompleteTodo(CompleteTodoInput input) {
        if (input == null) {
            return Result.fail("Input cannot be null");
        }
        if (input.getId() == null || input.getId().isBlank()) {
            return Result.fail("Todo ID cannot be empty");
        }
        return Result.ok(input);
    }

    // Domain logic
    private Result<Todo> createTodoHandler(CreateTodoInput input) {
        Todo todo = Todo.builder()
                .title(input.getTitle())
                .description(input.getDescription())
                .build();
        return Result.ok(repository.save(todo));
    }

    private Result<Todo> completeTodoHandler(CompleteTodoInput input) {
        return repository.findById(input.getId())
                .map(Todo::markComplete)
                .map(repository::save)
                .map(Result::ok)
                .orElse(Result.fail("Todo not found with ID: " + input.getId()));
    }

    private Result<List<Todo>> listTodosHandler(Object unused) {
        return Result.ok(repository.findAll());
    }

    /**
     * Create a new TodoApp with an in-memory repository.
     * 
     * @return A new TodoApp
     */
    public static TodoApp createInMemory() {
        return new TodoApp(new InMemoryTodoRepository());
    }
}
