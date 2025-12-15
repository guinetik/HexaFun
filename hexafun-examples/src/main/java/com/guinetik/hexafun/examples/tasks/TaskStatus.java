package com.guinetik.hexafun.examples.tasks;

/**
 * Task workflow status for Kanban-style management.
 */
public enum TaskStatus {
    /** Task is waiting to be started */
    TODO,
    /** Task is currently being worked on */
    DOING,
    /** Task has been completed */
    DONE;

    /**
     * Check if this status represents a completed task.
     */
    public boolean isCompleted() {
        return this == DONE;
    }

    /**
     * Check if this status represents an active task.
     */
    public boolean isActive() {
        return this == DOING;
    }
}
