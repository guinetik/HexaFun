package com.guinetik.hexafun.examples.todo;

import java.util.List;
import java.util.Optional;

/**
 * Output port for interacting with Todo storage.
 */
public interface TodoRepository {
    /**
     * Save a Todo item.
     * @param todo The Todo to save
     * @return The saved Todo (with any generated IDs)
     */
    Todo save(Todo todo);
    
    /**
     * Find a Todo by its ID.
     * @param id The Todo ID
     * @return The Todo, or empty if not found
     */
    Optional<Todo> findById(String id);
    
    /**
     * Get all Todo items.
     * @return A list of all Todos
     */
    List<Todo> findAll();
    
    /**
     * Delete a Todo by its ID.
     * @param id The Todo ID
     * @return true if deleted, false if not found
     */
    boolean deleteById(String id);
}
