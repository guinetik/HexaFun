package com.guinetik.hexafun.examples.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of TaskRepository.
 *
 * <p>This is an adapter that implements the output port.
 * Useful for testing and demos. In production, you'd have
 * a JPA, JDBC, or other persistent implementation.
 */
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        tasks.put(task.id(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public boolean delete(String id) {
        return tasks.remove(id) != null;
    }

    /**
     * Clear all tasks (useful for testing).
     */
    public void clear() {
        tasks.clear();
    }

    /**
     * Get the count of tasks.
     */
    public int count() {
        return tasks.size();
    }
}
