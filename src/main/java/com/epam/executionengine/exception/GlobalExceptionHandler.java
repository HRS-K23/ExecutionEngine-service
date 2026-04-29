package com.epam.executionengine.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

/**
 * Global exception handler for centralized error handling.
 * Maps exceptions to consistent HTTP responses.
 * 
 * @author EPMICMPCOD-300
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    /**
     * Handle access denied exceptions (403 Forbidden).
     * Triggered by @PostAuthorize violations.
     * 
     * @param ex the access denied exception
     * @return error response with 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(403)
            .error("Forbidden")
            .message("You do not have permission to access this resource")
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Handle authentication exceptions (401 Unauthorized).
     * Triggered by missing or invalid credentials.
     * 
     * @param ex the authentication exception
     * @return error response with 401 status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationError(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(401)
            .error("Unauthorized")
            .message("Invalid credentials or missing authentication token")
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Handle generic exceptions (500 Internal Server Error).
     * Catch-all for unexpected errors.
     * 
     * @param ex the exception
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(500)
            .error("Internal Server Error")
            .message("An unexpected error occurred while processing your request")
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
