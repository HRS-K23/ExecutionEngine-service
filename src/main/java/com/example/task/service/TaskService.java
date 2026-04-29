package com.example.task.service;

import com.example.task.dto.CreateTaskRequest;
import com.example.task.dto.UpdateTaskRequest;
import com.example.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    Task createTask(CreateTaskRequest request);

    Task getTaskById(Long taskId);

    Page<Task> listTasks(Pageable pageable);

    Task updateTask(Long taskId, UpdateTaskRequest request);

    void deleteTask(Long taskId);
}
