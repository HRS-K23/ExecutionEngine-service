package com.example.task.controller;

import com.example.task.entity.Task;
import com.example.task.exception.GlobalExceptionHandler;
import com.example.task.exception.TaskNotFoundException;
import com.example.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- POST /api/v1/tasks ---

    @Test
    void createTask_validInput_returns201() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("New Task");

        when(taskService.createTask(any())).thenReturn(task);

        String requestBody = "{\"title\":\"New Task\",\"description\":\"Desc\"}";

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void createTask_invalidInput_returns400() throws Exception {
        String requestBody = "{\"title\":\"\"}";

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_serviceThrows_returns500() throws Exception {
        when(taskService.createTask(any())).thenThrow(new RuntimeException("Unexpected error"));

        String requestBody = "{\"title\":\"Task\"}";

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/v1/tasks ---

    @Test
    void listTasks_validInput_returns200() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        Page<Task> page = new PageImpl<>(List.of(task));

        when(taskService.listTasks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void listTasks_emptyResult_returns200() throws Exception {
        when(taskService.listTasks(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void listTasks_serviceThrows_returns500() throws Exception {
        when(taskService.listTasks(any(Pageable.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/v1/tasks/{taskId} ---

    @Test
    void getTask_validInput_returns200() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Found Task");

        when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getTask_invalidInput_returns404() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new TaskNotFoundException("Task not found with id: 99"));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTask_serviceThrows_returns500() throws Exception {
        when(taskService.getTaskById(1L)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/v1/tasks/{taskId} ---

    @Test
    void updateTask_validInput_returns200() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Updated Task");

        when(taskService.updateTask(eq(1L), any())).thenReturn(task);

        String requestBody = "{\"title\":\"Updated Task\"}";

        mockMvc.perform(put("/api/v1/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void updateTask_entityNotFound_returns404() throws Exception {
        when(taskService.updateTask(eq(99L), any())).thenThrow(new TaskNotFoundException("Task not found with id: 99"));

        String requestBody = "{\"title\":\"Updated Task\"}";

        mockMvc.perform(put("/api/v1/tasks/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_serviceThrows_returns500() throws Exception {
        when(taskService.updateTask(eq(1L), any())).thenThrow(new RuntimeException("Unexpected error"));

        String requestBody = "{\"title\":\"Task\"}";

        mockMvc.perform(put("/api/v1/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/v1/tasks/{taskId} ---

    @Test
    void deleteTask_validInput_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    void deleteTask_entityNotFound_returns404() throws Exception {
        doThrow(new TaskNotFoundException("Task not found with id: 99"))
                .when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));

        verify(taskService, times(1)).deleteTask(99L);
    }

    @Test
    void deleteTask_serviceThrows_returns500() throws Exception {
        doThrow(new RuntimeException("Unexpected error")).when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).deleteTask(1L);
    }
}
