package com.guinetik.hexafun.examples.tasks;

import com.guinetik.hexafun.fun.Result;
import com.guinetik.hexafun.hexa.UseCaseKey;

import java.util.List;

import static com.guinetik.hexafun.examples.tasks.TaskInputs.*;

/**
 * Type-safe use case keys for task operations.
 */
public interface TaskUseCases {

    UseCaseKey<CreateTask, Result<Task>> CREATE =
        UseCaseKey.of("createTask");

    UseCaseKey<StartTask, Result<Task>> START =
        UseCaseKey.of("startTask");

    UseCaseKey<CompleteTask, Result<Task>> COMPLETE =
        UseCaseKey.of("completeTask");

    UseCaseKey<UpdateTask, Result<Task>> UPDATE =
        UseCaseKey.of("updateTask");

    UseCaseKey<DeleteTask, Result<Boolean>> DELETE =
        UseCaseKey.of("deleteTask");

    UseCaseKey<FindTask, Result<Task>> FIND =
        UseCaseKey.of("findTask");

    UseCaseKey<Void, List<Task>> LIST =
        UseCaseKey.of("listTasks");
}
