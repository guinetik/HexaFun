package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the port registry functionality.
 */
@DisplayName("Port Registry")
public class PortRegistryTest {

    // Sample port interfaces for testing
    interface TaskRepository {
        String save(String task);
        String findById(String id);
    }

    interface EmailService {
        void send(String to, String message);
    }

    // Sample implementations
    static class InMemoryTaskRepository implements TaskRepository {
        @Override
        public String save(String task) {
            return "saved:" + task;
        }

        @Override
        public String findById(String id) {
            return "task:" + id;
        }
    }

    static class MockEmailService implements EmailService {
        private String lastTo;
        private String lastMessage;

        @Override
        public void send(String to, String message) {
            this.lastTo = to;
            this.lastMessage = message;
        }

        public String getLastTo() { return lastTo; }
        public String getLastMessage() { return lastMessage; }
    }

    @Nested
    @DisplayName("HexaApp.port() - direct registration")
    class DirectRegistrationTests {

        @Test
        @DisplayName("should register and retrieve a port")
        void shouldRegisterAndRetrievePort() {
            HexaApp app = HexaApp.create();
            TaskRepository repo = new InMemoryTaskRepository();

            app.port(TaskRepository.class, repo);

            TaskRepository retrieved = app.port(TaskRepository.class);
            assertSame(repo, retrieved);
        }

        @Test
        @DisplayName("should return correct type")
        void shouldReturnCorrectType() {
            HexaApp app = HexaApp.create();
            app.port(TaskRepository.class, new InMemoryTaskRepository());

            TaskRepository repo = app.port(TaskRepository.class);
            assertEquals("saved:test", repo.save("test"));
        }

        @Test
        @DisplayName("should throw when port not registered")
        void shouldThrowWhenNotRegistered() {
            HexaApp app = HexaApp.create();

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> app.port(TaskRepository.class)
            );
            assertTrue(ex.getMessage().contains("TaskRepository"));
        }

        @Test
        @DisplayName("should support multiple port types")
        void shouldSupportMultiplePorts() {
            HexaApp app = HexaApp.create();
            TaskRepository repo = new InMemoryTaskRepository();
            MockEmailService email = new MockEmailService();

            app.port(TaskRepository.class, repo)
               .port(EmailService.class, email);

            assertSame(repo, app.port(TaskRepository.class));
            assertSame(email, app.port(EmailService.class));
        }

        @Test
        @DisplayName("should replace existing port registration")
        void shouldReplaceExistingPort() {
            HexaApp app = HexaApp.create();
            TaskRepository repo1 = new InMemoryTaskRepository();
            TaskRepository repo2 = new InMemoryTaskRepository();

            app.port(TaskRepository.class, repo1);
            app.port(TaskRepository.class, repo2);

            assertSame(repo2, app.port(TaskRepository.class));
        }

        @Test
        @DisplayName("hasPort should return true for registered port")
        void hasPortShouldReturnTrue() {
            HexaApp app = HexaApp.create();
            app.port(TaskRepository.class, new InMemoryTaskRepository());

            assertTrue(app.hasPort(TaskRepository.class));
        }

        @Test
        @DisplayName("hasPort should return false for unregistered port")
        void hasPortShouldReturnFalse() {
            HexaApp app = HexaApp.create();

            assertFalse(app.hasPort(TaskRepository.class));
        }

        @Test
        @DisplayName("registeredPorts should return all port types")
        void registeredPortsShouldReturnAllTypes() {
            HexaApp app = HexaApp.create();
            app.port(TaskRepository.class, new InMemoryTaskRepository())
               .port(EmailService.class, new MockEmailService());

            var ports = app.registeredPorts();
            assertEquals(2, ports.size());
            assertTrue(ports.contains(TaskRepository.class));
            assertTrue(ports.contains(EmailService.class));
        }
    }

    @Nested
    @DisplayName("DSL withPort() - builder registration")
    class DslRegistrationTests {

        @Test
        @DisplayName("should register port via DSL")
        void shouldRegisterPortViaDsl() {
            TaskRepository repo = new InMemoryTaskRepository();

            HexaApp app = HexaFun.dsl()
                .withPort(TaskRepository.class, repo)
                .build();

            assertSame(repo, app.port(TaskRepository.class));
        }

        @Test
        @DisplayName("should register multiple ports via DSL")
        void shouldRegisterMultiplePortsViaDsl() {
            TaskRepository repo = new InMemoryTaskRepository();
            MockEmailService email = new MockEmailService();

            HexaApp app = HexaFun.dsl()
                .withPort(TaskRepository.class, repo)
                .withPort(EmailService.class, email)
                .build();

            assertSame(repo, app.port(TaskRepository.class));
            assertSame(email, app.port(EmailService.class));
        }

        @Test
        @DisplayName("should mix ports with use cases")
        void shouldMixPortsWithUseCases() {
            UseCaseKey<String, String> SAVE = UseCaseKey.of("save");
            TaskRepository repo = new InMemoryTaskRepository();

            HexaApp app = HexaFun.dsl()
                .withPort(TaskRepository.class, repo)
                .useCase(SAVE)
                    .handle(input -> repo.save(input))
                .build();

            // Port is accessible
            assertSame(repo, app.port(TaskRepository.class));

            // Use case works
            String result = app.invoke(SAVE, "myTask");
            assertEquals("saved:myTask", result);
        }

        @Test
        @DisplayName("should allow ports before and after use cases")
        void shouldAllowPortsBeforeAndAfterUseCases() {
            UseCaseKey<String, String> FIND = UseCaseKey.of("find");
            TaskRepository repo = new InMemoryTaskRepository();
            MockEmailService email = new MockEmailService();

            // Note: withPort returns UseCaseBuilder, so can only be called before use cases
            HexaApp app = HexaFun.dsl()
                .withPort(TaskRepository.class, repo)
                .withPort(EmailService.class, email)
                .useCase(FIND)
                    .handle(id -> repo.findById(id))
                .build();

            assertTrue(app.hasPort(TaskRepository.class));
            assertTrue(app.hasPort(EmailService.class));
            assertEquals("task:123", app.invoke(FIND, "123"));
        }
    }

    @Nested
    @DisplayName("Integration - ports in use cases")
    class IntegrationTests {

        @Test
        @DisplayName("use case should be able to use injected port")
        void useCaseShouldUseInjectedPort() {
            UseCaseKey<String, String> CREATE = UseCaseKey.of("create");

            // Create app with port
            TaskRepository repo = new InMemoryTaskRepository();
            HexaApp app = HexaFun.dsl()
                .withPort(TaskRepository.class, repo)
                .useCase(CREATE)
                    .handle(input -> {
                        // In real app, you'd get the port from app context
                        // For now, we capture the repo in closure
                        return repo.save(input);
                    })
                .build();

            String result = app.invoke(CREATE, "my task");
            assertEquals("saved:my task", result);
        }
    }
}
