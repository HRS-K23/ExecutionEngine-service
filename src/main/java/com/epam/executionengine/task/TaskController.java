package com.epam.executionengine.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Task API endpoints.
 * Provides endpoints for retrieving task lists.
 * 
 * Base path: /api/v1/tasks
 * 
 * @author EPMICMPCOD-300
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * Get all tasks for the authenticated user.
     * 
     * Endpoint: GET /api/v1/tasks
     * Security: Requires USER role
     * 
     * @return ResponseEntity containing list of TaskDTOs
     */
    @GetMapping
    @PostAuthorize("hasRole('USER')")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        log.info("Fetching all tasks for user: {}", userId);
        List<TaskDTO> tasks = taskService.getTasksByUser(userId);
        log.info("Retrieved {} tasks for user: {}", tasks.size(), userId);
        
        return ResponseEntity.ok(tasks);
    }
}
