package com.guinetik.hexafun.examples.todo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A simple Todo item for our example.
 */
public record Todo(
        String id,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Todo {
        if (title == null) {
            throw new NullPointerException("Title is required");
        }
    }

    public static Builder builder() {
        return new Builder(null, null, null, false, null, null);
    }

    public Todo markComplete() {
        return new Todo(
                id,
                title,
                description,
                true,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Todo updateTitle(String newTitle) {
        return new Todo(
                id,
                newTitle,
                description,
                completed,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Todo updateDescription(String newDescription) {
        return new Todo(
                id,
                title,
                newDescription,
                completed,
                createdAt,
                LocalDateTime.now()
        );
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

    public static record Builder(
            String id,
            String title,
            String description,
            boolean completed,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public Builder id(String id) {
            return new Builder(id, title, description, completed, createdAt, updatedAt);
        }

        public Builder title(String title) {
            return new Builder(id, title, description, completed, createdAt, updatedAt);
        }

        public Builder description(String description) {
            return new Builder(id, title, description, completed, createdAt, updatedAt);
        }

        public Builder completed(boolean completed) {
            return new Builder(id, title, description, completed, createdAt, updatedAt);
        }

        public Builder createdAt(LocalDateTime createdAt) {
            return new Builder(id, title, description, completed, createdAt, updatedAt);
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            return new Builder(id, title, description, completed, createdAt, updatedAt);
        }

        public Todo build() {
            String finalId = id != null ? id : UUID.randomUUID().toString();
            LocalDateTime finalCreatedAt = createdAt != null ? createdAt : LocalDateTime.now();
            LocalDateTime finalUpdatedAt = updatedAt != null ? updatedAt : finalCreatedAt;
            
            return new Todo(finalId, title, description, completed, finalCreatedAt, finalUpdatedAt);
        }
    }
}
