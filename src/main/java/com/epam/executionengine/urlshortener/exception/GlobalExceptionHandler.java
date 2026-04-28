package com.epam.executionengine.urlshortener.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for the URL shortening service.
 * 
 * Centralizes exception handling across all REST endpoints, ensuring
 * consistent error responses and proper HTTP status codes.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles URLNotFoundException (HTTP 404).
     */
    @ExceptionHandler(URLNotFoundException.class)
    public ResponseEntity<RestApiErrorResponse> handleURLNotFoundException(
            URLNotFoundException ex,
            WebRequest request) {

        log.warn("URL not found: {}", ex.getMessage());

        RestApiErrorResponse errorResponse = RestApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles URLExpiredException (HTTP 410 Gone).
     */
    @ExceptionHandler(URLExpiredException.class)
    public ResponseEntity<RestApiErrorResponse> handleURLExpiredException(
            URLExpiredException ex,
            WebRequest request) {

        log.warn("URL expired: {}", ex.getMessage());

        RestApiErrorResponse errorResponse = RestApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.GONE.value())
                .error("Gone")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.GONE);
    }

    /**
     * Handles ShortURLException (HTTP 400 Bad Request).
     */
    @ExceptionHandler(ShortURLException.class)
    public ResponseEntity<RestApiErrorResponse> handleShortURLException(
            ShortURLException ex,
            WebRequest request) {

        log.warn("Short URL error: {}", ex.getMessage());

        RestApiErrorResponse errorResponse = RestApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation errors (HTTP 400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", message);

        RestApiErrorResponse errorResponse = RestApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Validation failed: " + message)
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles 404 for unknown endpoints (HTTP 404).
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<RestApiErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex,
            WebRequest request) {

        log.warn("Endpoint not found: {}", ex.getRequestURL());

        RestApiErrorResponse errorResponse = RestApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("Endpoint not found: " + ex.getRequestURL())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles all other unexpected exceptions (HTTP 500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestApiErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error occurred", ex);

        RestApiErrorResponse errorResponse = RestApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Extracts the request path from WebRequest.
     */
    private String getPath(WebRequest request) {
        String path = request.getDescription(false);
        return path != null ? path.replace("uri=", "") : "";
    }
}
