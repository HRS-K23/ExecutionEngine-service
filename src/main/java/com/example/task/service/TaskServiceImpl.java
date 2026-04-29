package com.example.task.service;

import com.example.task.entity.Task;
import com.example.task.entity.TaskStatus;
import com.example.task.dto.CreateTaskRequest;
import com.example.task.dto.UpdateTaskRequest;
import com.example.task.exception.TaskNotFoundException;
import com.example.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Override
    public Task createTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? 
            request.getPriority() : task.getPriority());
        return taskRepository.save(task);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(
                "Task not found with id: " + taskId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Task> listTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }
    
    @Override
    public Task updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = getTaskById(taskId);
        
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        
        return taskRepository.save(task);
    }
    
    @Override
    public void deleteTask(Long taskId) {
        Task task = getTaskById(taskId);
        taskRepository.delete(task);
    }
}
