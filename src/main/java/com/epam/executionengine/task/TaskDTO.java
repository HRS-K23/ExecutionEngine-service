package com.epam.executionengine.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Task responses.
 * Decouples API contracts from JPA entity changes.
 * 
 * @author EPMICMPCOD-300
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {
    
    private UUID id;
    private String title;
    private String description;
    private String userId;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
