package com.guinetik.hexafun.hexa;

import java.nio.file.DirectoryStream.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.guinetik.hexafun.fun.Result;

/**
 * Generic in-memory implementation of the HexaRepo interface.
 * Uses a HashMap to store entities with string IDs.
 * 
 * @param <T> The entity type this repository manages
 */
public class InMemoryHexaRepo<T> implements HexaRepo<T> {

    private final Map<String, T> storage = new HashMap<>();
    private final Function<T, String> idExtractor;
    private final Function<T, T> idGenerator;

    /**
     * Creates a new InMemoryHexaRepo with custom ID extraction and generation.
     * 
     * @param idExtractor Function to extract ID from an entity
     * @param idGenerator Function to generate a new ID for an entity that doesn't have one
     */
    public InMemoryHexaRepo(Function<T, String> idExtractor, Function<T, T> idGenerator) {
        this.idExtractor = idExtractor;
        this.idGenerator = idGenerator;
    }

    /**
     * Creates a new InMemoryHexaRepo with default UUID generation.
     * Note: This constructor assumes the entity has an ID field named "id" 
     * and is accessible via reflection or a setter.
     * 
     * @param idExtractor Function to extract ID from an entity
     */
    public InMemoryHexaRepo(Function<T, String> idExtractor) {
        this.idExtractor = idExtractor;
        this.idGenerator = entity -> {
            // This is a placeholder. In a real implementation, 
            // you would need to set the ID on the entity.
            // This requires knowing the entity structure.
            return entity;
        };
    }
    
    /**
     * Creates a new InMemoryHexaRepo with default behavior.
     * The idExtractor returns the ID from storage based on the entity.
     * The idGenerator simply returns the entity as is.
     */
    public InMemoryHexaRepo() {
        this.idExtractor = entity -> {
            for (Map.Entry<String, T> entry : storage.entrySet()) {
                if (entry.getValue().equals(entity)) {
                    return entry.getKey();
                }
            }
            return String.valueOf(storage.size() + 1);
        };
        this.idGenerator = entity -> entity;
    }

    //-------------------------------------------------------------------------
    // Create operations
    //-------------------------------------------------------------------------

    @Override
    public Result<T> save(T entity) {
        try {
            String id = idExtractor.apply(entity);
            
            // If no ID, generate one
            if (id == null || id.isEmpty()) {
                entity = idGenerator.apply(entity);
                id = idExtractor.apply(entity);
            }
            
            storage.put(id, entity);
            return Result.ok(entity);
        } catch (Exception e) {
            return Result.fail("Failed to save entity: " + e.getMessage());
        }
    }

    @Override
    public Result<List<T>> saveAll(List<T> entities) {
        try {
            List<T> savedEntities = new ArrayList<>();
            for (T entity : entities) {
                Result<T> result = save(entity);
                if (result.isFailure()) {
                    return Result.fail("Failed to save entities: " + result.error());
                }
                savedEntities.add(result.get());
            }
            return Result.ok(savedEntities);
        } catch (Exception e) {
            return Result.fail("Failed to save entities: " + e.getMessage());
        }
    }

    //-------------------------------------------------------------------------
    // Read operations
    //-------------------------------------------------------------------------

    @Override
    public Result<T> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Result.fail("ID cannot be null or empty");
        }
        
        T entity = storage.get(id);
        if (entity == null) {
            return Result.fail("Entity not found with ID: " + id);
        }
        
        return Result.ok(entity);
    }

    @Override
    public Result<List<T>> findAll() {
        return Result.ok(new ArrayList<>(storage.values()));
    }

    @Override
    public Result<List<T>> findBy(Filter<T> filter) {
        try {
            // Since DirectoryStream.Filter only has an accept method,
            // we need to create our own filtering logic
            List<T> result = storage.values().stream()
                .filter(entity -> {
                    try {
                        return filter.accept(entity);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
            
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("Failed to filter entities: " + e.getMessage());
        }
    }

    @Override
    public Result<List<T>> findAll(int offset, int limit) {
        if (offset < 0) {
            return Result.fail("Offset cannot be negative");
        }
        if (limit < 0) {
            return Result.fail("Limit cannot be negative");
        }
        
        List<T> allEntities = new ArrayList<>(storage.values());
        int fromIndex = Math.min(offset, allEntities.size());
        int toIndex = Math.min(offset + limit, allEntities.size());
        
        return Result.ok(allEntities.subList(fromIndex, toIndex));
    }

    @Override
    public Result<Long> count() {
        return Result.ok((long) storage.size());
    }

    //-------------------------------------------------------------------------
    // Update operations
    //-------------------------------------------------------------------------

    @Override
    public Result<T> update(String id, T entity) {
        if (id == null || id.isEmpty()) {
            return Result.fail("ID cannot be null or empty");
        }
        
        if (!storage.containsKey(id)) {
            return Result.fail("Cannot update: Entity not found with ID: " + id);
        }
        
        try {
            // Ensure the entity has the correct ID
            String entityId = idExtractor.apply(entity);
            if (entityId == null || !entityId.equals(id)) {
                return Result.fail("Entity ID doesn't match the provided ID");
            }
            
            storage.put(id, entity);
            return Result.ok(entity);
        } catch (Exception e) {
            return Result.fail("Failed to update entity: " + e.getMessage());
        }
    }

    //-------------------------------------------------------------------------
    // Delete operations
    //-------------------------------------------------------------------------

    @Override
    public Result<Boolean> deleteById(String id) {
        if (id == null || id.isEmpty()) {
            return Result.fail("ID cannot be null or empty");
        }
        
        if (!storage.containsKey(id)) {
            return Result.ok(false); // Entity doesn't exist, so technically delete succeeded
        }
        
        storage.remove(id);
        return Result.ok(true);
    }

    @Override
    public Result<Void> deleteAllById(List<String> ids) {
        if (ids == null) {
            return Result.fail("IDs list cannot be null");
        }
        
        for (String id : ids) {
            if (id != null && !id.isEmpty()) {
                storage.remove(id);
            }
        }
        
        return Result.ok(null);
    }

    @Override
    public Result<Void> clear() {
        storage.clear();
        return Result.ok(null);
    }
} 