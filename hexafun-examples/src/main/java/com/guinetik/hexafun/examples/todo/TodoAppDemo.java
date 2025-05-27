package com.guinetik.hexafun.examples.todo;

import com.guinetik.hexafun.fun.Result;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive demo application showing how to use the TodoApp example.
 * Allows users to create, complete, and list todos through a command-line interface.
 * 
 * This demo illustrates Hexagonal Architecture principles:
 * 
 * 1. Core domain: The Todo entity and business logic are isolated in the center
 * 2. Use cases: Creating, completing, and listing todos as primary operations
 * 3. Ports: TodoRepository as an output port for data persistence 
 * 4. Adapters: InMemoryTodoRepository as a secondary adapter implementation
 * 5. Input validation: Business rules enforced before domain operations
 * 6. Functional error handling: Using Result<T> for clean error management
 * 
 * The CLI interface here acts as a primary adapter, interacting with our domain
 * through the defined use cases without any direct knowledge of the domain model's
 * implementation details.
 */
public class TodoAppDemo {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Scanner scanner = new Scanner(System.in);
    private static TodoApp todoApp;
    
    public static void main(String[] args) {
        // Create a TodoApp with an in-memory repository
        todoApp = TodoApp.createInMemory();
        
        // Debug: Print registered use cases
        todoApp.printRegisteredUseCases();
        
        System.out.println("\n>> Welcome to TodoApp Demo!");
        System.out.println("This demo showcases HexaFun's Hexagonal Architecture implementation.");
        System.out.println("\n(i) About HexaFun:");
        System.out.println("- Functional Java framework for Hexagonal Architecture");
        System.out.println("- Clean DSL for defining use cases with .from().to() pipelines");
        System.out.println("- Separation of business logic from adapters");
        System.out.println("- Strong testing capabilities with fluent API");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1" -> createTodo();
                case "2" -> listTodos();
                case "3" -> completeTodo();
                case "4" -> running = false;
                default -> System.out.println("Invalid choice. Please try again.");
            }
            
            // Add a separator between commands
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                System.out.println("----------------------------------------");
            }
        }
        
        System.out.println("\n>> Thank you for trying the TodoApp Demo!");
        System.out.println("To learn more about HexaFun, check out the README.md file.");
    }
    
    private static void printMenu() {
        System.out.println("\n== TodoApp Menu:");
        System.out.println("1. (+) Create a new Todo");
        System.out.println("2. (#) List all Todos");
        System.out.println("3. (v) Complete a Todo");
        System.out.println("4. (x) Exit");
        System.out.print("\nEnter your choice (1-4): ");
    }
    
    private static void createTodo() {
        System.out.println("\n=== Create a new Todo ===");
        
        String title = "";
        while (title.isEmpty()) {
            System.out.print("Enter Todo title (required): ");
            title = scanner.nextLine().trim();
            
            if (title.isEmpty()) {
                System.out.println("Title cannot be empty. Please try again.");
            }
        }
        
        System.out.print("Enter Todo description (optional): ");
        String description = scanner.nextLine().trim();
        
        // Confirm creation
        System.out.println("\nTodo to create:");
        System.out.println("Title: " + title);
        System.out.println("Description: " + (description.isEmpty() ? "(None)" : description));
        System.out.print("Create this Todo? (y/n): ");
        
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("\nTodo creation cancelled.");
            return;
        }
        
        // Use the TodoApp to create a new Todo
        Result<Todo> result = todoApp.createTodo(new CreateTodoInput(title, description));
        
        // Handle the result
        if (result.isSuccess()) {
            Todo todo = result.get();
            System.out.println("\n(v) Todo created successfully!");
            printTodoDetails(todo);
        } else {
            System.out.println("\n(x) Failed to create Todo: " + result.error());
        }
    }
    
    private static void listTodos() {
        System.out.println("\n=== Todo List ===");
        
        // Use the TodoApp to list all Todos
        Result<List<Todo>> result = todoApp.listTodos();
        
        if (result.isSuccess()) {
            List<Todo> todos = result.get();
            
            if (todos.isEmpty()) {
                System.out.println("No todos found. Create some todos first!");
            } else {
                // Group todos by completion status
                List<Todo> incompleteTodos = todos.stream()
                        .filter(todo -> !todo.completed())
                        .toList();
                
                List<Todo> completedTodos = todos.stream()
                        .filter(todo -> todo.completed())
                        .toList();
                
                // Display incomplete todos first
                if (!incompleteTodos.isEmpty()) {
                    System.out.println("\n[>] PENDING TODOS:");
                    System.out.println("----------------------------------------");
                    for (int i = 0; i < incompleteTodos.size(); i++) {
                        Todo todo = incompleteTodos.get(i);
                        String createdAt = todo.createdAt().format(DATE_FORMAT);
                        System.out.printf("%d. %s | %s | Created: %s%n", 
                                i + 1,
                                todo.id().substring(0, 8), 
                                todo.title(),
                                createdAt);
                        
                        // Show description if present
                        if (todo.description() != null && !todo.description().isEmpty()) {
                            System.out.printf("   Description: %s%n", todo.description());
                        }
                    }
                }
                
                // Display completed todos
                if (!completedTodos.isEmpty()) {
                    System.out.println("\n[v] COMPLETED TODOS:");
                    System.out.println("----------------------------------------");
                    for (int i = 0; i < completedTodos.size(); i++) {
                        Todo todo = completedTodos.get(i);
                        String completedAt = todo.updatedAt().format(DATE_FORMAT);
                        System.out.printf("%d. %s | %s | Completed: %s%n", 
                                i + 1,
                                todo.id().substring(0, 8), 
                                todo.title(),
                                completedAt);
                    }
                }
                
                // Show summary
                System.out.println("\n[#] SUMMARY:");
                System.out.printf("Total: %d todo(s) | Pending: %d | Completed: %d%n", 
                        todos.size(), 
                        incompleteTodos.size(),
                        completedTodos.size());
            }
        } else {
            System.out.println("Failed to list Todos: " + result.error());
        }
    }
    
    private static void completeTodo() {
        System.out.println("\n=== Complete a Todo ===");
        
        // First list all incomplete todos
        Result<List<Todo>> listResult = todoApp.listTodos();
        
        if (listResult.isSuccess()) {
            List<Todo> incompleteTodos = listResult.get().stream()
                    .filter(todo -> !todo.completed())
                    .toList();
            
            if (incompleteTodos.isEmpty()) {
                System.out.println("No incomplete todos found. All todos are completed!");
                return;
            }
            
            System.out.println("Incomplete Todos:");
            System.out.println("ID | Title");
            System.out.println("----------------------------------------");
            
            for (int i = 0; i < incompleteTodos.size(); i++) {
                Todo todo = incompleteTodos.get(i);
                System.out.printf("%d. %s | %s%n", 
                        i + 1,
                        todo.id().substring(0, 8), 
                        todo.title());
            }
            
            System.out.print("\nEnter number of the Todo to complete (or leave empty to cancel): ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("Operation cancelled.");
                return;
            }
            
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < incompleteTodos.size()) {
                    Todo selectedTodo = incompleteTodos.get(index);
                    
                    // Use the TodoApp to complete the Todo
                    Result<Todo> result = todoApp.completeTodo(new CompleteTodoInput(selectedTodo.id()));
                    
                    if (result.isSuccess()) {
                        Todo completedTodo = result.get();
                        System.out.println("\n(v) Todo completed successfully!");
                        printTodoDetails(completedTodo);
                    } else {
                        System.out.println("\n(x) Failed to complete Todo: " + result.error());
                    }
                } else {
                    System.out.println("Invalid number. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        } else {
            System.out.println("Failed to list Todos: " + listResult.error());
        }
    }
    
    private static void printTodoDetails(Todo todo) {
        System.out.println("\nTodo Details:");
        System.out.println("ID: " + todo.id());
        System.out.println("Title: " + todo.title());
        System.out.println("Description: " + (todo.description().isEmpty() ? "(None)" : todo.description()));
        System.out.println("Status: " + (todo.completed() ? "Completed" : "Not completed"));
        System.out.println("Created: " + todo.createdAt().format(DATE_FORMAT));
        
        if (todo.completed()) {
            System.out.println("Completed: " + todo.updatedAt().format(DATE_FORMAT));
        }
    }
}
