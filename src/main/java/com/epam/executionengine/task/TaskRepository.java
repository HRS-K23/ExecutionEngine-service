package com.epam.executionengine.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Task entity.
 * Provides CRUD operations and custom query methods.
 * 
 * @author EPMICMPCOD-300
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    /**
     * Find all tasks for a specific user.
     * 
     * @param userId the user ID
     * @return list of tasks for the user
     */
    List<Task> findByUserId(String userId);
    
    /**
     * Find all tasks for a user with a specific status.
     * 
     * @param userId the user ID
     * @param status the task status
     * @return list of tasks matching criteria
     */
    List<Task> findByUserIdAndStatus(String userId, TaskStatus status);
}
