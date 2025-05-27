package com.guinetik.hexafun.hexa;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.DirectoryStream.Filter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.guinetik.hexafun.fun.Result;

/**
 * Unit tests for the InMemoryHexaRepo implementation.
 */
class InMemoryHexaRepoTest {

    // Test entity class
    static class TestEntity {
        private String id;
        private String name;
        
        public TestEntity(String name) {
            this.name = name;
        }
        
        public TestEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    private InMemoryHexaRepo<TestEntity> repo;
    
    @BeforeEach
    void setUp() {
        // Setup the repository with ID extractor and generator functions
        repo = new InMemoryHexaRepo<>(
            entity -> entity.getId(),
            entity -> {
                if (entity.getId() == null || entity.getId().isEmpty()) {
                    entity.setId(UUID.randomUUID().toString());
                }
                return entity;
            }
        );
    }
    
    // Create operation tests
    
    @Test
    void testSave_WithoutId_ShouldGenerateId() {
        // Arrange
        TestEntity entity = new TestEntity("Test Entity");
        
        // Act
        Result<TestEntity> result = repo.save(entity);
        
        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.get().getId());
        assertEquals("Test Entity", result.get().getName());
    }
    
    @Test
    void testSave_WithId_ShouldKeepSameId() {
        // Arrange
        String testId = "test-id-123";
        TestEntity entity = new TestEntity(testId, "Test Entity");
        
        // Act
        Result<TestEntity> result = repo.save(entity);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(testId, result.get().getId());
    }
    
    @Test
    void testSaveAll_MultipleEntities() {
        // Arrange
        List<TestEntity> entities = Arrays.asList(
            new TestEntity("Entity 1"),
            new TestEntity("Entity 2"),
            new TestEntity("Entity 3")
        );
        
        // Act
        Result<List<TestEntity>> result = repo.saveAll(entities);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(3, result.get().size());
        assertNotNull(result.get().get(0).getId());
        assertNotNull(result.get().get(1).getId());
        assertNotNull(result.get().get(2).getId());
    }
    
    // Read operation tests
    
    @Test
    void testFindById_ExistingEntity_ShouldReturnEntity() {
        // Arrange
        TestEntity entity = new TestEntity("test-id", "Test Entity");
        repo.save(entity);
        
        // Act
        Result<TestEntity> result = repo.findById("test-id");
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Test Entity", result.get().getName());
    }
    
    @Test
    void testFindById_NonExistentEntity_ShouldReturnFailure() {
        // Act
        Result<TestEntity> result = repo.findById("non-existent-id");
        
        // Assert
        assertTrue(result.isFailure());
        assertTrue(result.error().contains("not found"));
    }
    
    @Test
    void testFindAll_MultipleEntities() {
        // Arrange
        repo.save(new TestEntity("id1", "Entity 1"));
        repo.save(new TestEntity("id2", "Entity 2"));
        repo.save(new TestEntity("id3", "Entity 3"));
        
        // Act
        Result<List<TestEntity>> result = repo.findAll();
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(3, result.get().size());
    }
    
    @Test
    void testFindAll_EmptyRepo_ShouldReturnEmptyList() {
        // Act
        Result<List<TestEntity>> result = repo.findAll();
        
        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.get().isEmpty());
    }
    
    @Test
    void testFindBy_MatchingEntities() {
        // Arrange
        repo.save(new TestEntity("id1", "Apple"));
        repo.save(new TestEntity("id2", "Banana"));
        repo.save(new TestEntity("id3", "Apple Pie"));
        
        // Create a filter that finds entities containing "Apple"
        Filter<TestEntity> filter = entity -> entity.getName().contains("Apple");
        
        // Act
        Result<List<TestEntity>> result = repo.findBy(filter);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.get().size());
    }
    
    @Test
    void testFindAll_WithPagination() {
        // Arrange
        for (int i = 1; i <= 10; i++) {
            repo.save(new TestEntity("id" + i, "Entity " + i));
        }
        
        // Act - get entities 4-6 (skip 3, take 3)
        Result<List<TestEntity>> result = repo.findAll(3, 3);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(3, result.get().size());
    }
    
    @Test
    void testCount_MultipleEntities() {
        // Arrange
        repo.save(new TestEntity("id1", "Entity 1"));
        repo.save(new TestEntity("id2", "Entity 2"));
        
        // Act
        Result<Long> result = repo.count();
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2L, result.get());
    }
    
    // Update operation tests
    
    @Test
    void testUpdate_ExistingEntity() {
        // Arrange
        String testId = "test-id";
        repo.save(new TestEntity(testId, "Original Name"));
        
        // Create updated entity
        TestEntity updatedEntity = new TestEntity(testId, "Updated Name");
        
        // Act
        Result<TestEntity> result = repo.update(testId, updatedEntity);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Updated Name", result.get().getName());
        
        // Verify the update was persisted
        Result<TestEntity> findResult = repo.findById(testId);
        assertEquals("Updated Name", findResult.get().getName());
    }
    
    @Test
    void testUpdate_NonExistentEntity_ShouldFail() {
        // Arrange
        TestEntity entity = new TestEntity("non-existent-id", "Test Entity");
        
        // Act
        Result<TestEntity> result = repo.update("non-existent-id", entity);
        
        // Assert
        assertTrue(result.isFailure());
        assertTrue(result.error().contains("not found"));
    }
    
    // Delete operation tests
    
    @Test
    void testDeleteById_ExistingEntity_ShouldReturnTrue() {
        // Arrange
        String testId = "test-id";
        repo.save(new TestEntity(testId, "Test Entity"));
        
        // Act
        Result<Boolean> result = repo.deleteById(testId);
        
        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.get());
        
        // Verify the entity was deleted
        Result<TestEntity> findResult = repo.findById(testId);
        assertTrue(findResult.isFailure());
    }
    
    @Test
    void testDeleteById_NonExistentEntity_ShouldReturnFalse() {
        // Act
        Result<Boolean> result = repo.deleteById("non-existent-id");
        
        // Assert
        assertTrue(result.isSuccess());
        assertFalse(result.get());
    }
    
    @Test
    void testDeleteAllById_MultipleEntities() {
        // Arrange
        repo.save(new TestEntity("id1", "Entity 1"));
        repo.save(new TestEntity("id2", "Entity 2"));
        repo.save(new TestEntity("id3", "Entity 3"));
        
        // Act
        Result<Void> result = repo.deleteAllById(Arrays.asList("id1", "id3"));
        
        // Assert
        assertTrue(result.isSuccess());
        
        // Verify entities were deleted
        assertEquals(1, repo.findAll().get().size());
        assertTrue(repo.findById("id1").isFailure());
        assertTrue(repo.findById("id3").isFailure());
        assertTrue(repo.findById("id2").isSuccess());
    }
    
    @Test
    void testClear_ShouldRemoveAllEntities() {
        // Arrange
        repo.save(new TestEntity("id1", "Entity 1"));
        repo.save(new TestEntity("id2", "Entity 2"));
        
        // Act
        Result<Void> result = repo.clear();
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(0, repo.findAll().get().size());
    }
} 