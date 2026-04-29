package com.epam.executionengine.task;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a task.
 * Mapped to 'tasks' table with performance indexes.
 * 
 * @author EPMICMPCOD-300
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_status", columnList = "user_id,status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
