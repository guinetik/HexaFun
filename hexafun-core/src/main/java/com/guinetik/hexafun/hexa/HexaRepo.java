package com.guinetik.hexafun.hexa;

import java.nio.file.DirectoryStream.Filter;
import java.util.List;

import com.guinetik.hexafun.fun.Result;

/**
 * Generic repository interface for entity persistence operations.
 * Provides CRUD (Create, Read, Update, Delete) operations with Result wrappers.
 * 
 * @param <T> The entity type this repository manages
 */
public interface HexaRepo<T> {
    //-------------------------------------------------------------------------
    // Create operations
    //-------------------------------------------------------------------------
    
    /**
     * Saves a single entity to the repository.
     * 
     * @param entity The entity to save
     * @return A Result containing the saved entity with any generated values
     */
    Result<T> save(T entity);
    
    /**
     * Saves multiple entities to the repository in a batch operation.
     * 
     * @param entities The list of entities to save
     * @return A Result containing the list of saved entities
     */
    Result<List<T>> saveAll(List<T> entities);
    
    //-------------------------------------------------------------------------
    // Read operations
    //-------------------------------------------------------------------------
    
    /**
     * Finds an entity by its unique identifier.
     * 
     * @param id The unique identifier of the entity
     * @return A Result containing the found entity or an error if not found
     */
    Result<T> findById(String id);
    
    /**
     * Retrieves all entities from the repository.
     * 
     * @return A Result containing a list of all entities
     */
    Result<List<T>> findAll();
    
    /**
     * Retrieves entities that match the given filter criteria.
     * 
     * @param filter The filter to apply
     * @return A Result containing a list of matching entities
     */
    Result<List<T>> findBy(Filter<T> filter);
    
    /**
     * Retrieves a paginated list of entities.
     * 
     * @param offset The number of entities to skip
     * @param limit The maximum number of entities to return
     * @return A Result containing a list of entities within the specified range
     */
    Result<List<T>> findAll(int offset, int limit);
    
    /**
     * Counts the total number of entities in the repository.
     * 
     * @return A Result containing the count of entities
     */
    Result<Long> count();
    
    //-------------------------------------------------------------------------
    // Update operations
    //-------------------------------------------------------------------------
    
    /**
     * Updates an existing entity identified by its ID.
     * 
     * @param id The ID of the entity to update
     * @param entity The updated entity data
     * @return A Result containing the updated entity
     */
    Result<T> update(String id, T entity);
    
    //-------------------------------------------------------------------------
    // Delete operations
    //-------------------------------------------------------------------------
    
    /**
     * Deletes an entity by its unique identifier.
     * 
     * @param id The unique identifier of the entity to delete
     * @return A Result containing a Boolean indicating success
     */
    Result<Boolean> deleteById(String id);
    
    /**
     * Deletes multiple entities by their IDs in a batch operation.
     * 
     * @param ids The list of entity IDs to delete
     * @return A Result indicating the operation outcome
     */
    Result<Void> deleteAllById(List<String> ids);
    
    /**
     * Removes all entities from the repository.
     * 
     * @return A Result indicating the operation outcome
     */
    Result<Void> clear();
}
