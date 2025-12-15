package com.guinetik.hexafun.examples.tasks;

import java.util.UUID;

/**
 * Immutable Task domain model with Kanban workflow status.
 */
public record Task(
    String id,
    String title,
    String description,
    TaskStatus status
) {
    /**
     * Create a new task with generated ID in TODO status.
     */
    public static Task create(String title, String description) {
        return new Task(UUID.randomUUID().toString(), title, description, TaskStatus.TODO);
    }

    /**
     * Check if this task is completed.
     */
    public boolean completed() {
        return status == TaskStatus.DONE;
    }

    /**
     * Start working on this task (move to DOING).
     */
    public Task start() {
        return new Task(id, title, description, TaskStatus.DOING);
    }

    /**
     * Mark this task as completed (move to DONE).
     */
    public Task complete() {
        return new Task(id, title, description, TaskStatus.DONE);
    }

    /**
     * Move task back to TODO.
     */
    public Task reopen() {
        return new Task(id, title, description, TaskStatus.TODO);
    }

    /**
     * Update the title.
     */
    public Task withTitle(String newTitle) {
        return new Task(id, newTitle, description, status);
    }

    /**
     * Update the description.
     */
    public Task withDescription(String newDescription) {
        return new Task(id, title, newDescription, status);
    }

    /**
     * Update the status.
     */
    public Task withStatus(TaskStatus newStatus) {
        return new Task(id, title, description, newStatus);
    }
}
