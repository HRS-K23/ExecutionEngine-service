package com.epam.executionengine.urlshortener.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Standardized error response format for REST API errors.
 * 
 * Provides consistent error information across all endpoints, including
 * timestamp, HTTP status, error type, message, and request path.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestApiErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Short error type/name (e.g., "Bad Request", "Not Found").
     */
    private String error;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * The request path that caused the error.
     */
    private String path;

    /**
     * Optional: detailed trace for debugging (usually in development only).
     */
    private String trace;
}
