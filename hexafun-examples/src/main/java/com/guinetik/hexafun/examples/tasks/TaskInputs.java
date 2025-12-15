package com.guinetik.hexafun.examples.tasks;

/**
 * Input records for task use cases.
 */
public interface TaskInputs {

    /**
     * Input for creating a task.
     */
    record CreateTask(String title, String description) {}

    /**
     * Input for starting a task (moving to DOING).
     */
    record StartTask(String taskId) {}

    /**
     * Input for completing a task (moving to DONE).
     */
    record CompleteTask(String taskId) {}

    /**
     * Input for updating a task.
     */
    record UpdateTask(String taskId, String title, String description) {}

    /**
     * Input for deleting a task.
     */
    record DeleteTask(String taskId) {}

    /**
     * Input for finding a task.
     */
    record FindTask(String taskId) {}
}
