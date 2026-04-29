package com.epam.executionengine.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskMapper entity-to-DTO conversion logic.
 * Tests mapping between Task entities and TaskDTOs.
 */
@DisplayName("TaskMapper Tests")
class TaskMapperTest {
    
    private TaskMapper taskMapper;
    private Task task;
    private UUID taskId;
    
    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();
        taskId = UUID.randomUUID();
        
        task = Task.builder()
            .id(taskId)
            .title("Sample Task")
            .description("Sample Description")
            .userId("user1")
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("toDTO_validTask_convertsSuccessfully")
    void testToDTO_validTask_convertsSuccessfully() {
        // Act
        TaskDTO result = taskMapper.toDTO(task);
        
        // Assert
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getDescription(), result.getDescription());
        assertEquals(task.getUserId(), result.getUserId());
        assertEquals(task.getStatus(), result.getStatus());
        assertEquals(task.getCreatedAt(), result.getCreatedAt());
        assertEquals(task.getUpdatedAt(), result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("toDTO_nullTask_returnsNull")
    void testToDTO_nullTask_returnsNull() {
        // Act
        TaskDTO result = taskMapper.toDTO(null);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("toDTO_taskWithoutDescription_convertsSuccessfully")
    void testToDTO_taskWithoutDescription_convertsSuccessfully() {
        // Arrange
        task.setDescription(null);
        
        // Act
        TaskDTO result = taskMapper.toDTO(task);
        
        // Assert
        assertNotNull(result);
        assertNull(result.getDescription());
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getUserId(), result.getUserId());
    }
    
    @Test
    @DisplayName("toDTO_preservesAllStatuses_mapsCorrectly")
    void testToDTO_preservesAllStatuses_mapsCorrectly() {
        // Test PENDING
        task.setStatus(TaskStatus.PENDING);
        TaskDTO dto1 = taskMapper.toDTO(task);
        assertEquals(TaskStatus.PENDING, dto1.getStatus());
        
        // Test IN_PROGRESS
        task.setStatus(TaskStatus.IN_PROGRESS);
        TaskDTO dto2 = taskMapper.toDTO(task);
        assertEquals(TaskStatus.IN_PROGRESS, dto2.getStatus());
        
        // Test COMPLETED
        task.setStatus(TaskStatus.COMPLETED);
        TaskDTO dto3 = taskMapper.toDTO(task);
        assertEquals(TaskStatus.COMPLETED, dto3.getStatus());
    }
    
    @Test
    @DisplayName("toDTOList_validTaskList_convertsSuccessfully")
    void testToDTOList_validTaskList_convertsSuccessfully() {
        // Arrange
        UUID taskId2 = UUID.randomUUID();
        Task task2 = Task.builder()
            .id(taskId2)
            .title("Another Task")
            .description("Another Description")
            .userId("user2")
            .status(TaskStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        List<Task> tasks = Arrays.asList(task, task2);
        
        // Act
        List<TaskDTO> result = taskMapper.toDTOList(tasks);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(task.getId(), result.get(0).getId());
        assertEquals(task2.getId(), result.get(1).getId());
        assertEquals("Sample Task", result.get(0).getTitle());
        assertEquals("Another Task", result.get(1).getTitle());
    }
    
    @Test
    @DisplayName("toDTOList_emptyList_returnsEmptyList")
    void testToDTOList_emptyList_returnsEmptyList() {
        // Act
        List<TaskDTO> result = taskMapper.toDTOList(Collections.emptyList());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("toDTOList_nullList_returnsEmptyList")
    void testToDTOList_nullList_returnsEmptyList() {
        // Act
        List<TaskDTO> result = taskMapper.toDTOList(null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("toDTOList_singleTask_returnsListWithOneDTO")
    void testToDTOList_singleTask_returnsListWithOneDTO() {
        // Arrange
        List<Task> tasks = Collections.singletonList(task);
        
        // Act
        List<TaskDTO> result = taskMapper.toDTOList(tasks);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(task.getId(), result.get(0).getId());
    }
    
    @Test
    @DisplayName("toDTOList_preservesOrder_mapsInSequence")
    void testToDTOList_preservesOrder_mapsInSequence() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        
        Task task1 = Task.builder()
            .id(id1)
            .title("Task 1")
            .userId("user1")
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Task task2 = Task.builder()
            .id(id2)
            .title("Task 2")
            .userId("user1")
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Task task3 = Task.builder()
            .id(id3)
            .title("Task 3")
            .userId("user1")
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        List<Task> tasks = Arrays.asList(task1, task2, task3);
        
        // Act
        List<TaskDTO> result = taskMapper.toDTOList(tasks);
        
        // Assert
        assertEquals(3, result.size());
        assertEquals(id1, result.get(0).getId());
        assertEquals(id2, result.get(1).getId());
        assertEquals(id3, result.get(2).getId());
    }
}
