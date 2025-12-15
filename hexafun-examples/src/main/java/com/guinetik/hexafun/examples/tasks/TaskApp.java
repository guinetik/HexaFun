package com.guinetik.hexafun.examples.tasks;

import static com.guinetik.hexafun.examples.tasks.TaskInputs.*;
import static com.guinetik.hexafun.examples.tasks.TaskUseCases.*;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import com.guinetik.hexafun.fun.Result;
import java.util.List;

/**
 * Task application demonstrating the port registry feature.
 *
 * <p>Key concepts demonstrated:
 * <ul>
 *   <li>Port registration: {@code .withPort(TaskRepository.class, impl)}</li>
 *   <li>Port retrieval: {@code app.port(TaskRepository.class)}</li>
 *   <li>Use cases using ports for persistence</li>
 *   <li>Separation of domain logic from infrastructure</li>
 * </ul>
 *
 * <p>The repository is injected as a port, allowing easy swapping
 * between in-memory (for testing) and persistent implementations.
 */
public class TaskApp {

    private final HexaApp app;

    /**
     * Create a TaskApp with the given repository implementation.
     */
    public TaskApp(TaskRepository repository) {
        this.app = HexaFun.dsl()
            // Register the repository as a port
            .withPort(TaskRepository.class, repository)
            // CREATE: validate title, then save
            .useCase(CREATE)
            .validate(TaskValidators::validateCreateTitle)
            .validate(TaskValidators::validateCreateTitleLength)
            .handle(input -> {
                Task task = Task.create(input.title(), input.description());
                return Result.ok(repository.save(task));
            })
            // START: validate ID, find task, move to DOING
            .useCase(START)
            .validate(TaskValidators::validateStartTaskId)
            .handle(input ->
                repository
                    .findById(input.taskId())
                    .map(task -> Result.ok(repository.save(task.start())))
                    .orElse(Result.fail("Task not found: " + input.taskId()))
            )
            // COMPLETE: validate ID, find task, move to DONE
            .useCase(COMPLETE)
            .validate(TaskValidators::validateCompleteTaskId)
            .handle(input ->
                repository
                    .findById(input.taskId())
                    .map(task -> Result.ok(repository.save(task.complete())))
                    .orElse(Result.fail("Task not found: " + input.taskId()))
            )
            // UPDATE: validate ID and title, find and update
            .useCase(UPDATE)
            .validate(TaskValidators::validateUpdateTaskId)
            .validate(TaskValidators::validateUpdateTitle)
            .handle(input ->
                repository
                    .findById(input.taskId())
                    .map(task -> {
                        Task updated = task
                            .withTitle(input.title())
                            .withDescription(input.description());
                        return Result.ok(repository.save(updated));
                    })
                    .orElse(Result.fail("Task not found: " + input.taskId()))
            )
            // DELETE: validate ID, delete from repo
            .useCase(DELETE)
            .validate(TaskValidators::validateDeleteTaskId)
            .handle(input -> Result.ok(repository.delete(input.taskId())))
            // FIND: validate ID, look up in repo
            .useCase(FIND)
            .validate(TaskValidators::validateFindTaskId)
            .handle(input ->
                repository
                    .findById(input.taskId())
                    .map(Result::ok)
                    .orElse(Result.fail("Task not found: " + input.taskId()))
            )
            // LIST: no validation needed, just return all
            .useCase(LIST)
            .handle(input -> repository.findAll())
            .build();
    }

    /**
     * Create a TaskApp with an in-memory repository.
     */
    public static TaskApp withInMemoryRepo() {
        return new TaskApp(new InMemoryTaskRepository());
    }

    // ----- Public API -----

    public Result<Task> createTask(String title, String description) {
        return app.invoke(CREATE, new CreateTask(title, description));
    }

    public Result<Task> startTask(String taskId) {
        return app.invoke(START, new StartTask(taskId));
    }

    public Result<Task> completeTask(String taskId) {
        return app.invoke(COMPLETE, new CompleteTask(taskId));
    }

    public Result<Task> updateTask(
        String taskId,
        String title,
        String description
    ) {
        return app.invoke(UPDATE, new UpdateTask(taskId, title, description));
    }

    public Result<Boolean> deleteTask(String taskId) {
        return app.invoke(DELETE, new DeleteTask(taskId));
    }

    public Result<Task> findTask(String taskId) {
        return app.invoke(FIND, new FindTask(taskId));
    }

    public List<Task> listTasks() {
        return app.invoke(LIST, null);
    }

    /**
     * Get the underlying HexaApp (for testing/introspection).
     */
    public HexaApp getApp() {
        return app;
    }

    /**
     * Get the repository port directly.
     */
    public TaskRepository getRepository() {
        return app.port(TaskRepository.class);
    }

    // ----- Demo -----

    public static void main(String[] args) {
        System.out.println("=== TaskApp Demo ===\n");

        // Create app with in-memory repository
        TaskApp taskApp = TaskApp.withInMemoryRepo();

        // Show that the port is registered
        System.out.println(
            "Registered ports: " + taskApp.getApp().registeredPorts()
        );
        System.out.println(
            "Has TaskRepository: " +
                taskApp.getApp().hasPort(TaskRepository.class)
        );
        System.out.println();

        // Create some tasks
        System.out.println("Creating tasks...");
        Result<Task> task1 = taskApp.createTask(
            "Learn HexaFun",
            "Study the fluent DSL"
        );
        Result<Task> task2 = taskApp.createTask(
            "Write tests",
            "Add comprehensive tests"
        );
        Result<Task> task3 = taskApp.createTask(
            "Deploy app",
            "Push to production"
        );

        System.out.println("Created: " + task1.get().title());
        System.out.println("Created: " + task2.get().title());
        System.out.println("Created: " + task3.get().title());
        System.out.println();

        // List all tasks
        System.out.println("All tasks:");
        taskApp
            .listTasks()
            .forEach(t ->
                System.out.println(
                    "  - [" + (t.completed() ? "X" : " ") + "] " + t.title()
                )
            );
        System.out.println();

        // Complete a task
        String taskId = task1.get().id();
        System.out.println("Completing task: " + task1.get().title());
        taskApp.completeTask(taskId);
        System.out.println();

        // List again to see the change
        System.out.println("Tasks after completion:");
        taskApp
            .listTasks()
            .forEach(t ->
                System.out.println(
                    "  - [" + (t.completed() ? "X" : " ") + "] " + t.title()
                )
            );
        System.out.println();

        // Try validation - empty title
        System.out.println("Trying to create task with empty title...");
        Result<Task> invalid = taskApp.createTask("", "No title");
        System.out.println(
            "Result: " +
                (invalid.isSuccess() ? "OK" : "FAILED: " + invalid.error())
        );
        System.out.println();

        // Try validation - title too long
        System.out.println("Trying to create task with very long title...");
        String longTitle = "A".repeat(101);
        Result<Task> tooLong = taskApp.createTask(longTitle, "Too long");
        System.out.println(
            "Result: " +
                (tooLong.isSuccess() ? "OK" : "FAILED: " + tooLong.error())
        );
        System.out.println();

        // Find a task
        System.out.println("Finding task by ID: " + taskId);
        Result<Task> found = taskApp.findTask(taskId);
        System.out.println(
            "Found: " +
                found.get().title() +
                " (completed: " +
                found.get().completed() +
                ")"
        );
        System.out.println();

        // Delete a task
        System.out.println("Deleting task: " + task3.get().title());
        taskApp.deleteTask(task3.get().id());
        System.out.println("Tasks remaining: " + taskApp.listTasks().size());
    }
}
