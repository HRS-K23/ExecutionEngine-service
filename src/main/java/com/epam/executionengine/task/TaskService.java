package com.epam.executionengine.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Task business logic.
 * Handles task retrieval, filtering, and DTO conversion.
 * 
 * @author EPMICMPCOD-300
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    
    /**
     * Retrieve all tasks for a specific user.
     * 
     * @param userId the user ID
     * @return list of task DTOs for the user
     */
    public List<TaskDTO> getTasksByUser(String userId) {
        log.debug("Retrieving tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findByUserId(userId);
        log.debug("Found {} tasks for user: {}", tasks.size(), userId);
        return taskMapper.toDTOList(tasks);
    }
    
    /**
     * Retrieve tasks for a specific user filtered by status.
     * 
     * @param userId the user ID
     * @param status the task status to filter by
     * @return list of task DTOs matching criteria
     */
    public List<TaskDTO> getTasksByUserAndStatus(String userId, TaskStatus status) {
        log.debug("Retrieving tasks for user: {} with status: {}", userId, status);
        List<Task> tasks = taskRepository.findByUserIdAndStatus(userId, status);
        log.debug("Found {} tasks for user: {} with status: {}", tasks.size(), userId, status);
        return taskMapper.toDTOList(tasks);
    }
}
