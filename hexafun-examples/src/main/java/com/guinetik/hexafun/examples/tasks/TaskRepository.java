package com.guinetik.hexafun.examples.tasks;

import java.util.List;
import java.util.Optional;

/**
 * Output port for task persistence.
 *
 * <p>This is the interface that use cases depend on.
 * Implementations (adapters) provide the actual storage mechanism.
 */
public interface TaskRepository {

    /**
     * Save a task (create or update).
     * @param task The task to save
     * @return The saved task
     */
    Task save(Task task);

    /**
     * Find a task by ID.
     * @param id The task ID
     * @return The task if found
     */
    Optional<Task> findById(String id);

    /**
     * Find all tasks.
     * @return List of all tasks
     */
    List<Task> findAll();

    /**
     * Delete a task by ID.
     * @param id The task ID
     * @return true if deleted, false if not found
     */
    boolean delete(String id);

    /**
     * Check if a task exists.
     * @param id The task ID
     * @return true if exists
     */
    default boolean exists(String id) {
        return findById(id).isPresent();
    }
}
