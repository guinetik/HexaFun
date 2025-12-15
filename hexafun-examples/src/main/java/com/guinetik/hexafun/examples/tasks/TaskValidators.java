package com.guinetik.hexafun.examples.tasks;

import com.guinetik.hexafun.fun.Result;

import static com.guinetik.hexafun.examples.tasks.TaskInputs.*;

/**
 * Validation functions for task inputs.
 */
public class TaskValidators {

    // --- CreateTask validators ---

    public static Result<CreateTask> validateCreateTitle(CreateTask input) {
        if (input.title() == null || input.title().isBlank()) {
            return Result.fail("Title cannot be empty");
        }
        return Result.ok(input);
    }

    public static Result<CreateTask> validateCreateTitleLength(CreateTask input) {
        if (input.title().length() > 100) {
            return Result.fail("Title cannot exceed 100 characters");
        }
        return Result.ok(input);
    }

    // --- StartTask validators ---

    public static Result<StartTask> validateStartTaskId(StartTask input) {
        if (input.taskId() == null || input.taskId().isBlank()) {
            return Result.fail("Task ID cannot be empty");
        }
        return Result.ok(input);
    }

    // --- CompleteTask validators ---

    public static Result<CompleteTask> validateCompleteTaskId(CompleteTask input) {
        if (input.taskId() == null || input.taskId().isBlank()) {
            return Result.fail("Task ID cannot be empty");
        }
        return Result.ok(input);
    }

    // --- UpdateTask validators ---

    public static Result<UpdateTask> validateUpdateTaskId(UpdateTask input) {
        if (input.taskId() == null || input.taskId().isBlank()) {
            return Result.fail("Task ID cannot be empty");
        }
        return Result.ok(input);
    }

    public static Result<UpdateTask> validateUpdateTitle(UpdateTask input) {
        if (input.title() == null || input.title().isBlank()) {
            return Result.fail("Title cannot be empty");
        }
        return Result.ok(input);
    }

    // --- DeleteTask validators ---

    public static Result<DeleteTask> validateDeleteTaskId(DeleteTask input) {
        if (input.taskId() == null || input.taskId().isBlank()) {
            return Result.fail("Task ID cannot be empty");
        }
        return Result.ok(input);
    }

    // --- FindTask validators ---

    public static Result<FindTask> validateFindTaskId(FindTask input) {
        if (input.taskId() == null || input.taskId().isBlank()) {
            return Result.fail("Task ID cannot be empty");
        }
        return Result.ok(input);
    }
}
