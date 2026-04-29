package com.epam.executionengine.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TaskController REST endpoints.
 * Tests HTTP request/response handling with MockMvc and security.
 */
@WebMvcTest(TaskController.class)
@DisplayName("TaskController Tests")
class TaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TaskService taskService;
    
    private TaskDTO taskDTO;
    private UUID taskId;
    
    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        
        taskDTO = TaskDTO.builder()
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
    @DisplayName("getAllTasks_validRequest_returns200")
    @WithMockUser(roles = "USER")
    void testGetAllTasks_validRequest_returns200() throws Exception {
        // Arrange
        List<TaskDTO> tasks = Collections.singletonList(taskDTO);
        when(taskService.getTasksByUser(anyString())).thenReturn(tasks);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks")
                .with(httpBasic("user1", "password123")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Test Task")))
            .andExpect(jsonPath("$[0].userId", is("user1")))
            .andExpect(jsonPath("$[0].status", is("PENDING")));
        
        verify(taskService, times(1)).getTasksByUser(anyString());
    }
    
    @Test
    @DisplayName("getAllTasks_noTasksFound_returns200Empty")
    @WithMockUser(roles = "USER")
    void testGetAllTasks_noTasksFound_returns200Empty() throws Exception {
        // Arrange
        when(taskService.getTasksByUser(anyString())).thenReturn(Collections.emptyList());
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks")
                .with(httpBasic("user1", "password123")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$", hasSize(0)));
        
        verify(taskService, times(1)).getTasksByUser(anyString());
    }
    
    @Test
    @DisplayName("getAllTasks_serviceThrowsException_returns500")
    @WithMockUser(roles = "USER")
    void testGetAllTasks_serviceThrowsException_returns500() throws Exception {
        // Arrange
        when(taskService.getTasksByUser(anyString()))
            .thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks")
                .with(httpBasic("user1", "password123")))
            .andExpect(status().isInternalServerError());
        
        verify(taskService, times(1)).getTasksByUser(anyString());
    }
    
    @Test
    @DisplayName("getAllTasks_unauthenticated_returns401")
    void testGetAllTasks_unauthenticated_returns401() throws Exception {
        // Act & Assert - No authentication provided
        mockMvc.perform(get("/api/v1/tasks"))
            .andExpect(status().isUnauthorized());
        
        verify(taskService, times(0)).getTasksByUser(anyString());
    }
    
    @Test
    @DisplayName("getAllTasks_invalidCredentials_returns401")
    void testGetAllTasks_invalidCredentials_returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks")
                .with(httpBasic("user1", "wrongpassword")))
            .andExpect(status().isUnauthorized());
        
        verify(taskService, times(0)).getTasksByUser(anyString());
    }
    
    @Test
    @DisplayName("getAllTasks_multipleTasksReturned_validatesAllFields")
    @WithMockUser(roles = "USER")
    void testGetAllTasks_multipleTasksReturned_validatesAllFields() throws Exception {
        // Arrange
        UUID taskId2 = UUID.randomUUID();
        TaskDTO taskDTO2 = TaskDTO.builder()
            .id(taskId2)
            .title("Second Task")
            .description("Second Description")
            .userId("user1")
            .status(TaskStatus.IN_PROGRESS)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        List<TaskDTO> tasks = List.of(taskDTO, taskDTO2);
        when(taskService.getTasksByUser(anyString())).thenReturn(tasks);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks")
                .with(httpBasic("user1", "password123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title", is("Test Task")))
            .andExpect(jsonPath("$[1].title", is("Second Task")))
            .andExpect(jsonPath("$[1].status", is("IN_PROGRESS")));
        
        verify(taskService, times(1)).getTasksByUser(anyString());
    }
    
    @Test
    @DisplayName("getAllTasks_withValidUser_extractsUserIdCorrectly")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetAllTasks_withValidUser_extractsUserIdCorrectly() throws Exception {
        // Arrange
        when(taskService.getTasksByUser("testuser")).thenReturn(Collections.singletonList(taskDTO));
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks"))
            .andExpect(status().isOk());
        
        verify(taskService, times(1)).getTasksByUser("testuser");
    }
}
