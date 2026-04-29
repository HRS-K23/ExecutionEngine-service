package com.epam.executionengine.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService business logic.
 * Tests service layer methods with mocked repository and mapper.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tests")
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @InjectMocks
    private TaskService taskService;
    
    private Task sampleTask;
    private TaskDTO sampleTaskDTO;
    private UUID taskId;
    
    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        
        sampleTask = Task.builder()
            .id(taskId)
            .title("Test Task")
            .description("Test Description")
            .userId("user1")
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        sampleTaskDTO = TaskDTO.builder()
            .id(taskId)
            .title("Test Task")
            .description("Test Description")
            .userId("user1")
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("getTasksByUser_validInput_returnsExpected")
    void testGetTasksByUser_validInput_returnsExpected() {
        // Arrange
        String userId = "user1";
        List<Task> tasks = Collections.singletonList(sampleTask);
        List<TaskDTO> taskDTOs = Collections.singletonList(sampleTaskDTO);
        
        when(taskRepository.findByUserId(userId)).thenReturn(tasks);
        when(taskMapper.toDTOList(tasks)).thenReturn(taskDTOs);
        
        // Act
        List<TaskDTO> result = taskService.getTasksByUser(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleTaskDTO.getId(), result.get(0).getId());
        assertEquals(sampleTaskDTO.getTitle(), result.get(0).getTitle());
        
        verify(taskRepository, times(1)).findByUserId(userId);
        verify(taskMapper, times(1)).toDTOList(tasks);
    }
    
    @Test
    @DisplayName("getTasksByUser_noTasksFound_returnsEmpty")
    void testGetTasksByUser_noTasksFound_returnsEmpty() {
        // Arrange
        String userId = "user1";
        when(taskRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(taskMapper.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());
        
        // Act
        List<TaskDTO> result = taskService.getTasksByUser(userId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(taskRepository, times(1)).findByUserId(userId);
        verify(taskMapper, times(1)).toDTOList(Collections.emptyList());
    }
    
    @Test
    @DisplayName("getTasksByUser_nullUserId_throwsException")
    void testGetTasksByUser_nullUserId_throwsException() {
        // Arrange
        when(taskRepository.findByUserId(null)).thenReturn(Collections.emptyList());
        when(taskMapper.toDTOList(any())).thenReturn(Collections.emptyList());
        
        // Act & Assert
        assertDoesNotThrow(() -> taskService.getTasksByUser(null));
        
        verify(taskRepository, times(1)).findByUserId(null);
    }
    
    @Test
    @DisplayName("getTasksByUserAndStatus_validInput_returnsExpected")
    void testGetTasksByUserAndStatus_validInput_returnsExpected() {
        // Arrange
        String userId = "user1";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        List<Task> tasks = Collections.singletonList(sampleTask);
        List<TaskDTO> taskDTOs = Collections.singletonList(sampleTaskDTO);
        
        when(taskRepository.findByUserIdAndStatus(userId, status)).thenReturn(tasks);
        when(taskMapper.toDTOList(tasks)).thenReturn(taskDTOs);
        
        // Act
        List<TaskDTO> result = taskService.getTasksByUserAndStatus(userId, status);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(taskRepository, times(1)).findByUserIdAndStatus(userId, status);
        verify(taskMapper, times(1)).toDTOList(tasks);
    }
    
    @Test
    @DisplayName("getTasksByUserAndStatus_noMatchingStatus_returnsEmpty")
    void testGetTasksByUserAndStatus_noMatchingStatus_returnsEmpty() {
        // Arrange
        String userId = "user1";
        TaskStatus status = TaskStatus.COMPLETED;
        when(taskRepository.findByUserIdAndStatus(userId, status)).thenReturn(Collections.emptyList());
        when(taskMapper.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());
        
        // Act
        List<TaskDTO> result = taskService.getTasksByUserAndStatus(userId, status);
        
        // Assert
        assertTrue(result.isEmpty());
        
        verify(taskRepository, times(1)).findByUserIdAndStatus(userId, status);
    }
    
    @Test
    @DisplayName("getTasksByUserAndStatus_nullStatus_throwsException")
    void testGetTasksByUserAndStatus_nullStatus_throwsException() {
        // Arrange
        String userId = "user1";
        when(taskRepository.findByUserIdAndStatus(userId, null)).thenReturn(Collections.emptyList());
        when(taskMapper.toDTOList(any())).thenReturn(Collections.emptyList());
        
        // Act & Assert
        assertDoesNotThrow(() -> taskService.getTasksByUserAndStatus(userId, null));
        
        verify(taskRepository, times(1)).findByUserIdAndStatus(userId, null);
    }
}
