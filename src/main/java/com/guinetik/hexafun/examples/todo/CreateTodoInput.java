package com.guinetik.hexafun.examples.todo;

/**
 * Input data for creating a Todo.
 */
public class CreateTodoInput {
    private final String title;
    private final String description;

    public CreateTodoInput(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
