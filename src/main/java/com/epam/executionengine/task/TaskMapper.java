package com.epam.executionengine.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Task entity and TaskDTO.
 * Provides entity-to-DTO and list conversion methods.
 * 
 * @author EPMICMPCOD-300
 */
@Component
@Slf4j
public class TaskMapper {
    
    /**
     * Convert Task entity to TaskDTO.
     * 
     * @param task the task entity
     * @return the task DTO or null if input is null
     */
    public TaskDTO toDTO(Task task) {
        if (task == null) {
            return null;
        }
        
        return TaskDTO.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .userId(task.getUserId())
            .status(task.getStatus())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
    
    /**
     * Convert list of Task entities to list of TaskDTOs.
     * 
     * @param tasks the task entities list
     * @return the task DTOs list or empty list if input is null
     */
    public List<TaskDTO> toDTOList(List<Task> tasks) {
        if (tasks == null) {
            return Collections.emptyList();
        }
        
        return tasks.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}
