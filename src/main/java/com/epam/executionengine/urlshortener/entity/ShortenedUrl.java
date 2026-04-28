package com.epam.executionengine.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a shortened URL mapping.
 * 
 * This entity maintains the mapping between a short code and the original long URL,
 * along with metadata such as creation timestamp, expiration date, and access count.
 */
@Entity
@Table(name = "shortened_urls", indexes = {
    @Index(name = "idx_short_code", columnList = "short_code", unique = true),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_expires_at", columnList = "expires_at"),
    @Index(name = "idx_created_by", columnList = "created_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ShortenedUrl {

    /**
     * Unique identifier (UUID) for this shortened URL record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The short code that represents this URL (e.g., "aB3xY9").
     * Must be unique and URL-safe (Base62 alphanumeric).
     */
    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;

    /**
     * The original long URL that this short code maps to.
     * Stored as TEXT to support URLs up to 8000+ characters.
     */
    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;

    /**
     * Timestamp when this shortened URL was created.
     * Set once at creation and never updated.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Optional expiration timestamp. 
     * If NULL, the URL never expires.
     * If set, the URL is considered "Gone" (HTTP 410) after this timestamp.
     */
    @Column(name = "expires_at", nullable = true)
    private LocalDateTime expiresAt;

    /**
     * Number of times this short URL has been accessed (redirected).
     * Incremented on each successful redirect.
     */
    @Column(name = "access_count", nullable = false)
    @Builder.Default
    private Long accessCount = 0L;

    /**
     * Identifier of the user or service that created this shortened URL.
     * Used for audit trail and ownership tracking.
     */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    /**
     * Pre-persist callback to set the creation timestamp if not already set.
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.accessCount == null) {
            this.accessCount = 0L;
        }
    }

    /**
     * Check if this shortened URL has expired.
     * 
     * @return true if expiresAt is set and has passed, false otherwise
     */
    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false; // Never expires
        }
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
