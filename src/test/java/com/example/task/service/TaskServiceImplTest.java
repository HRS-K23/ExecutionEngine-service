package com.example.task.service;

import com.example.task.dto.CreateTaskRequest;
import com.example.task.dto.UpdateTaskRequest;
import com.example.task.entity.Task;
import com.example.task.entity.TaskPriority;
import com.example.task.entity.TaskStatus;
import com.example.task.exception.TaskNotFoundException;
import com.example.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    // --- createTask ---

    @Test
    void createTask_validInput_returnsExpected() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Description");
        request.setPriority(TaskPriority.HIGH);

        Task saved = new Task();
        saved.setId(1L);
        saved.setTitle("Test Task");

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        Task result = taskService.createTask(request);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_nullPriority_usesDefault() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("No Priority Task");

        Task saved = new Task();
        saved.setId(2L);
        saved.setTitle("No Priority Task");
        saved.setPriority(TaskPriority.MEDIUM);

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        Task result = taskService.createTask(request);

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_invalidInput_throwsException() {
        when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("DB error"));

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Fail Task");

        assertThrows(RuntimeException.class, () -> taskService.createTask(request));
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // --- getTaskById ---

    @Test
    void getTaskById_validInput_returnsExpected() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Found Task");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_entityNotFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(99L));
        verify(taskRepository, times(1)).findById(99L);
    }

    @Test
    void getTaskById_invalidInput_throwsException() {
        when(taskRepository.findById(null)).thenThrow(new IllegalArgumentException("ID must not be null"));

        assertThrows(IllegalArgumentException.class, () -> taskService.getTaskById(null));
    }

    // --- listTasks ---

    @Test
    void listTasks_validInput_returnsExpected() {
        Task task = new Task();
        task.setId(1L);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(pageable)).thenReturn(page);

        Page<Task> result = taskService.listTasks(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(taskRepository, times(1)).findAll(pageable);
    }

    @Test
    void listTasks_entityNotFound_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(taskRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<Task> result = taskService.listTasks(pageable);

        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findAll(pageable);
    }

    @Test
    void listTasks_invalidInput_throwsException() {
        when(taskRepository.findAll((Pageable) null)).thenThrow(new IllegalArgumentException("Pageable must not be null"));

        assertThrows(IllegalArgumentException.class, () -> taskService.listTasks(null));
    }

    // --- updateTask ---

    @Test
    void updateTask_validInput_returnsExpected() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Old Title");
        existing.setStatus(TaskStatus.PENDING);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New Title");
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenReturn(existing);

        Task result = taskService.updateTask(1L, request);

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_entityNotFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New Title");

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(99L, request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_invalidInput_throwsException() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Title");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("DB error"));

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("X");

        assertThrows(RuntimeException.class, () -> taskService.updateTask(1L, request));
    }

    @Test
    void updateTask_completedStatus_setsCompletedAt() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Task");
        existing.setStatus(TaskStatus.PENDING);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateTask(1L, request);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // --- deleteTask ---

    @Test
    void deleteTask_validInput_returnsExpected() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("To Delete");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        assertDoesNotThrow(() -> taskService.deleteTask(1L));

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_entityNotFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(99L));

        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void deleteTask_invalidInput_throwsException() {
        when(taskRepository.findById(null)).thenThrow(new IllegalArgumentException("ID must not be null"));

        assertThrows(IllegalArgumentException.class, () -> taskService.deleteTask(null));
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
