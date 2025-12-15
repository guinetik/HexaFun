package com.guinetik.hexafun.examples.tasks;

import com.guinetik.hexafun.fun.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.guinetik.hexafun.examples.tasks.TaskInputs.*;
import static com.guinetik.hexafun.examples.tasks.TaskUseCases.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TaskApp demonstrating port registry usage.
 */
@DisplayName("TaskApp")
public class TaskAppTest {

    private TaskApp taskApp;
    private InMemoryTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
        taskApp = new TaskApp(repository);
    }

    @Nested
    @DisplayName("Port Registry")
    class PortRegistryTests {

        @Test
        @DisplayName("should register TaskRepository port")
        void shouldRegisterPort() {
            assertTrue(taskApp.getApp().hasPort(TaskRepository.class));
        }

        @Test
        @DisplayName("should retrieve same repository instance")
        void shouldRetrieveSameInstance() {
            TaskRepository retrieved = taskApp.getApp().port(TaskRepository.class);
            assertSame(repository, retrieved);
        }

        @Test
        @DisplayName("should list registered ports")
        void shouldListPorts() {
            var ports = taskApp.getApp().registeredPorts();
            assertEquals(1, ports.size());
            assertTrue(ports.contains(TaskRepository.class));
        }

        @Test
        @DisplayName("getRepository should return the port")
        void getRepositoryShouldReturnPort() {
            assertSame(repository, taskApp.getRepository());
        }
    }

    @Nested
    @DisplayName("CREATE use case")
    class CreateTests {

        @Test
        @DisplayName("should create task with valid input")
        void shouldCreateTask() {
            Result<Task> result = taskApp.createTask("Test Task", "Description");

            assertTrue(result.isSuccess());
            assertEquals("Test Task", result.get().title());
            assertEquals("Description", result.get().description());
            assertFalse(result.get().completed());
            assertNotNull(result.get().id());
        }

        @Test
        @DisplayName("should persist task in repository")
        void shouldPersistTask() {
            Result<Task> result = taskApp.createTask("Test Task", "Description");

            assertEquals(1, repository.count());
            assertTrue(repository.findById(result.get().id()).isPresent());
        }

        @Test
        @DisplayName("should fail with empty title")
        void shouldFailWithEmptyTitle() {
            Result<Task> result = taskApp.createTask("", "Description");

            assertTrue(result.isFailure());
            assertEquals("Title cannot be empty", result.error());
        }

        @Test
        @DisplayName("should fail with null title")
        void shouldFailWithNullTitle() {
            Result<Task> result = taskApp.createTask(null, "Description");

            assertTrue(result.isFailure());
            assertEquals("Title cannot be empty", result.error());
        }

        @Test
        @DisplayName("should fail with title exceeding 100 characters")
        void shouldFailWithLongTitle() {
            String longTitle = "A".repeat(101);
            Result<Task> result = taskApp.createTask(longTitle, "Description");

            assertTrue(result.isFailure());
            assertEquals("Title cannot exceed 100 characters", result.error());
        }

        @Test
        @DisplayName("should accept title with exactly 100 characters")
        void shouldAcceptMaxLengthTitle() {
            String maxTitle = "A".repeat(100);
            Result<Task> result = taskApp.createTask(maxTitle, "Description");

            assertTrue(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("COMPLETE use case")
    class CompleteTests {

        @Test
        @DisplayName("should complete existing task")
        void shouldCompleteTask() {
            Task task = repository.save(Task.create("Test", "Desc"));

            Result<Task> result = taskApp.completeTask(task.id());

            assertTrue(result.isSuccess());
            assertTrue(result.get().completed());
        }

        @Test
        @DisplayName("should persist completed status")
        void shouldPersistCompletedStatus() {
            Task task = repository.save(Task.create("Test", "Desc"));

            taskApp.completeTask(task.id());

            Task stored = repository.findById(task.id()).orElseThrow();
            assertTrue(stored.completed());
        }

        @Test
        @DisplayName("should fail for non-existent task")
        void shouldFailForNonExistent() {
            Result<Task> result = taskApp.completeTask("non-existent-id");

            assertTrue(result.isFailure());
            assertTrue(result.error().contains("Task not found"));
        }

        @Test
        @DisplayName("should fail with empty task ID")
        void shouldFailWithEmptyId() {
            Result<Task> result = taskApp.completeTask("");

            assertTrue(result.isFailure());
            assertEquals("Task ID cannot be empty", result.error());
        }
    }

    @Nested
    @DisplayName("UPDATE use case")
    class UpdateTests {

        @Test
        @DisplayName("should update task title and description")
        void shouldUpdateTask() {
            Task task = repository.save(Task.create("Old Title", "Old Desc"));

            Result<Task> result = taskApp.updateTask(task.id(), "New Title", "New Desc");

            assertTrue(result.isSuccess());
            assertEquals("New Title", result.get().title());
            assertEquals("New Desc", result.get().description());
        }

        @Test
        @DisplayName("should fail for non-existent task")
        void shouldFailForNonExistent() {
            Result<Task> result = taskApp.updateTask("non-existent", "Title", "Desc");

            assertTrue(result.isFailure());
            assertTrue(result.error().contains("Task not found"));
        }

        @Test
        @DisplayName("should fail with empty title")
        void shouldFailWithEmptyTitle() {
            Task task = repository.save(Task.create("Old", "Desc"));

            Result<Task> result = taskApp.updateTask(task.id(), "", "Desc");

            assertTrue(result.isFailure());
            assertEquals("Title cannot be empty", result.error());
        }
    }

    @Nested
    @DisplayName("DELETE use case")
    class DeleteTests {

        @Test
        @DisplayName("should delete existing task")
        void shouldDeleteTask() {
            Task task = repository.save(Task.create("Test", "Desc"));

            Result<Boolean> result = taskApp.deleteTask(task.id());

            assertTrue(result.isSuccess());
            assertTrue(result.get());
            assertEquals(0, repository.count());
        }

        @Test
        @DisplayName("should return false for non-existent task")
        void shouldReturnFalseForNonExistent() {
            Result<Boolean> result = taskApp.deleteTask("non-existent");

            assertTrue(result.isSuccess());
            assertFalse(result.get());
        }

        @Test
        @DisplayName("should fail with empty task ID")
        void shouldFailWithEmptyId() {
            Result<Boolean> result = taskApp.deleteTask("");

            assertTrue(result.isFailure());
            assertEquals("Task ID cannot be empty", result.error());
        }
    }

    @Nested
    @DisplayName("FIND use case")
    class FindTests {

        @Test
        @DisplayName("should find existing task")
        void shouldFindTask() {
            Task task = repository.save(Task.create("Test", "Desc"));

            Result<Task> result = taskApp.findTask(task.id());

            assertTrue(result.isSuccess());
            assertEquals(task.id(), result.get().id());
        }

        @Test
        @DisplayName("should fail for non-existent task")
        void shouldFailForNonExistent() {
            Result<Task> result = taskApp.findTask("non-existent");

            assertTrue(result.isFailure());
            assertTrue(result.error().contains("Task not found"));
        }
    }

    @Nested
    @DisplayName("LIST use case")
    class ListTests {

        @Test
        @DisplayName("should return empty list when no tasks")
        void shouldReturnEmptyList() {
            List<Task> tasks = taskApp.listTasks();

            assertTrue(tasks.isEmpty());
        }

        @Test
        @DisplayName("should return all tasks")
        void shouldReturnAllTasks() {
            repository.save(Task.create("Task 1", "Desc 1"));
            repository.save(Task.create("Task 2", "Desc 2"));
            repository.save(Task.create("Task 3", "Desc 3"));

            List<Task> tasks = taskApp.listTasks();

            assertEquals(3, tasks.size());
        }
    }

    @Nested
    @DisplayName("Testing DSL")
    class TestingDslTests {

        @Test
        @DisplayName("should test CREATE with expectFailure")
        void shouldTestCreateWithExpectFailure() {
            taskApp.getApp().test(CREATE)
                .with(new CreateTask("", "Description"))
                .expectFailure(error -> {
                    assertEquals("Title cannot be empty", error);
                });
        }

        @Test
        @DisplayName("should test LIST")
        void shouldTestList() {
            repository.save(Task.create("Task 1", "Desc"));
            repository.save(Task.create("Task 2", "Desc"));

            taskApp.getApp().test(LIST)
                .with(null)
                .expectOk(tasks -> {
                    assertEquals(2, tasks.size());
                });
        }
    }

    @Nested
    @DisplayName("Swappable Repository")
    class SwappableRepositoryTests {

        @Test
        @DisplayName("should work with different repository implementations")
        void shouldWorkWithDifferentRepos() {
            // Create a mock repository that always returns a specific task
            TaskRepository mockRepo = new TaskRepository() {
                @Override
                public Task save(Task task) {
                    return task;
                }

                @Override
                public java.util.Optional<Task> findById(String id) {
                    return java.util.Optional.of(new Task(id, "Mock Task", "Mock", TaskStatus.TODO));
                }

                @Override
                public List<Task> findAll() {
                    return List.of(new Task("1", "Mock Task", "Mock", TaskStatus.TODO));
                }

                @Override
                public boolean delete(String id) {
                    return true;
                }
            };

            // Create app with mock repository
            TaskApp mockApp = new TaskApp(mockRepo);

            // Verify the mock is used
            Result<Task> result = mockApp.findTask("any-id");
            assertTrue(result.isSuccess());
            assertEquals("Mock Task", result.get().title());
        }
    }
}
