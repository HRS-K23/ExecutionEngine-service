package com.epam.executionengine.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for short URL creation.
 * 
 * This DTO contains the complete information about a successfully created short URL,
 * including the generated short code, the full short URL, and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateShortUrlResponse {

    /**
     * The unique identifier (UUID) of the shortened URL record.
     */
    private UUID id;

    /**
     * The generated or provided short code (e.g., "aB3xY9").
     */
    private String shortCode;

    /**
     * The full short URL that can be shared (e.g., "https://exe.local/s/aB3xY9").
     */
    private String shortUrl;

    /**
     * The original long URL that was shortened.
     */
    private String longUrl;

    /**
     * Timestamp when the shortened URL was created.
     */
    private LocalDateTime createdAt;

    /**
     * Expiration timestamp, if set. Null indicates the URL never expires.
     */
    private LocalDateTime expiresAt;

    /**
     * Current access count for this shortened URL.
     */
    private Long accessCount;

    /**
     * Identifier of the user/service that created this shortened URL.
     */
    private String createdBy;
}
