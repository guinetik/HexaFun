package com.guinetik.hexafun.examples.todo;

/**
 * Input data for completing a Todo.
 */
public class CompleteTodoInput {
    private final String id;

    public CompleteTodoInput(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
