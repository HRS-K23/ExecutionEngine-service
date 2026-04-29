package com.epam.executionengine.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TaskRepository using @DataJpaTest.
 * Tests Spring Data JPA derived queries with in-memory H2 database.
 */
@DataJpaTest
@DisplayName("TaskRepository Tests")
class TaskRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TaskRepository taskRepository;
    
    private UUID taskId1;
    private UUID taskId2;
    private UUID taskId3;
    
    @BeforeEach
    void setUp() {
        taskId1 = UUID.randomUUID();
        taskId2 = UUID.randomUUID();
        taskId3 = UUID.randomUUID();
        
        // Create test tasks
        Task task1 = Task.builder()
            .id(taskId1)
            .title("Task 1")
            .description("Description 1")
            .userId("user1")
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Task task2 = Task.builder()
            .id(taskId2)
            .title("Task 2")
            .description("Description 2")
            .userId("user1")
            .status(TaskStatus.IN_PROGRESS)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Task task3 = Task.builder()
            .id(taskId3)
            .title("Task 3")
            .description("Description 3")
            .userId("user2")
            .status(TaskStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("findByUserId_existingUser_returnsTaskList")
    void testFindByUserId_existingUser_returnsTaskList() {
        // Act
        List<Task> tasks = taskRepository.findByUserId("user1");
        
        // Assert
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().allMatch(t -> t.getUserId().equals("user1")));
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 1")));
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 2")));
    }
    
    @Test
    @DisplayName("findByUserId_noTasksForUser_returnsEmpty")
    void testFindByUserId_noTasksForUser_returnsEmpty() {
        // Act
        List<Task> tasks = taskRepository.findByUserId("nonexistentUser");
        
        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test
    @DisplayName("findByUserId_nullUserId_returnsEmpty")
    void testFindByUserId_nullUserId_returnsEmpty() {
        // Act
        List<Task> tasks = taskRepository.findByUserId(null);
        
        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test
    @DisplayName("findByUserIdAndStatus_existingUserAndStatus_returnsFilteredTasks")
    void testFindByUserIdAndStatus_existingUserAndStatus_returnsFilteredTasks() {
        // Act
        List<Task> tasks = taskRepository.findByUserIdAndStatus("user1", TaskStatus.IN_PROGRESS);
        
        // Assert
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("user1", tasks.get(0).getUserId());
        assertEquals(TaskStatus.IN_PROGRESS, tasks.get(0).getStatus());
        assertEquals("Task 2", tasks.get(0).getTitle());
    }
    
    @Test
    @DisplayName("findByUserIdAndStatus_noMatchingCriteria_returnsEmpty")
    void testFindByUserIdAndStatus_noMatchingCriteria_returnsEmpty() {
        // Act
        List<Task> tasks = taskRepository.findByUserIdAndStatus("user1", TaskStatus.COMPLETED);
        
        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test
    @DisplayName("findByUserIdAndStatus_nullStatus_returnsEmpty")
    void testFindByUserIdAndStatus_nullStatus_returnsEmpty() {
        // Act
        List<Task> tasks = taskRepository.findByUserIdAndStatus("user1", null);
        
        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test
    @DisplayName("save_newTask_persistsToDatabase")
    void testSave_newTask_persistsToDatabase() {
        // Arrange
        UUID newTaskId = UUID.randomUUID();
        Task newTask = Task.builder()
            .id(newTaskId)
            .title("New Task")
            .description("New Description")
            .userId("user3")
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // Act
        Task savedTask = taskRepository.save(newTask);
        
        // Assert
        assertNotNull(savedTask);
        assertEquals(newTaskId, savedTask.getId());
        assertEquals("user3", savedTask.getUserId());
        
        Optional<Task> retrievedTask = taskRepository.findById(newTaskId);
        assertTrue(retrievedTask.isPresent());
        assertEquals("New Task", retrievedTask.get().getTitle());
    }
    
    @Test
    @DisplayName("findById_existingTask_returnsTask")
    void testFindById_existingTask_returnsTask() {
        // Act
        Optional<Task> task = taskRepository.findById(taskId1);
        
        // Assert
        assertTrue(task.isPresent());
        assertEquals("Task 1", task.get().getTitle());
        assertEquals("user1", task.get().getUserId());
    }
    
    @Test
    @DisplayName("findById_nonexistentTask_returnsEmpty")
    void testFindById_nonexistentTask_returnsEmpty() {
        // Act
        Optional<Task> task = taskRepository.findById(UUID.randomUUID());
        
        // Assert
        assertTrue(task.isEmpty());
    }
}
