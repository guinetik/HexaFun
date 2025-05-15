package com.guinetik.hexafun.examples.todo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of TodoRepository for testing.
 */
public class InMemoryTodoRepository implements TodoRepository {
    private final Map<String, Todo> todos = new HashMap<>();

    @Override
    public Todo save(Todo todo) {
        todos.put(todo.getId(), todo);
        return todo;
    }

    @Override
    public Optional<Todo> findById(String id) {
        return Optional.ofNullable(todos.get(id));
    }

    @Override
    public List<Todo> findAll() {
        return new ArrayList<>(todos.values());
    }

    @Override
    public boolean deleteById(String id) {
        return todos.remove(id) != null;
    }
    
    /**
     * Clear all todos from the repository.
     */
    public void clear() {
        todos.clear();
    }
}
