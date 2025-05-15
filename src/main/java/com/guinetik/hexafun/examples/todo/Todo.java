package com.guinetik.hexafun.examples.todo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A simple Todo item for our example.
 */
public class Todo {
    private final String id;
    private final String title;
    private final String description;
    private final boolean completed;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Todo(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.title = Objects.requireNonNull(builder.title, "Title is required");
        this.description = builder.description;
        this.completed = builder.completed;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Todo markComplete() {
        return new Builder()
                .id(id)
                .title(title)
                .description(description)
                .completed(true)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Todo updateTitle(String newTitle) {
        return new Builder()
                .id(id)
                .title(newTitle)
                .description(description)
                .completed(completed)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Todo updateDescription(String newDescription) {
        return new Builder()
                .id(id)
                .title(title)
                .description(newDescription)
                .completed(completed)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return completed == todo.completed &&
                Objects.equals(id, todo.id) &&
                Objects.equals(title, todo.title) &&
                Objects.equals(description, todo.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, completed);
    }

    @Override
    public String toString() {
        return "Todo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String title;
        private String description;
        private boolean completed;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Todo build() {
            return new Todo(this);
        }
    }
}
