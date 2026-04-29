package com.epam.executionengine.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler centralized error handling.
 * Tests exception mapping to HTTP responses.
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }
    
    @Test
    @DisplayName("handleAccessDenied_accessDeniedException_returns403")
    void testHandleAccessDenied_accessDeniedException_returns403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(403, body.getStatus());
        assertEquals("Forbidden", body.getError());
        assertTrue(body.getMessage().contains("permission"));
        assertNotNull(body.getTimestamp());
    }
    
    @Test
    @DisplayName("handleAccessDenied_includesTimestamp_notNull")
    void testHandleAccessDenied_includesTimestamp_notNull() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        LocalDateTime beforeCall = LocalDateTime.now();
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception);
        LocalDateTime afterCall = LocalDateTime.now();
        
        // Assert
        ErrorResponse body = response.getBody();
        assertNotNull(body.getTimestamp());
        assertTrue(body.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(body.getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }
    
    @Test
    @DisplayName("handleAuthenticationError_authenticationException_returns401")
    void testHandleAuthenticationError_authenticationException_returns401() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Invalid credentials") {};
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationError(exception);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(401, body.getStatus());
        assertEquals("Unauthorized", body.getError());
        assertTrue(body.getMessage().contains("credentials") || body.getMessage().contains("authentication"));
    }
    
    @Test
    @DisplayName("handleAuthenticationError_missingCredentials_returns401")
    void testHandleAuthenticationError_missingCredentials_returns401() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Missing token") {};
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationError(exception);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
    }
    
    @Test
    @DisplayName("handleGenericException_runtimeException_returns500")
    void testHandleGenericException_runtimeException_returns500() {
        // Arrange
        RuntimeException exception = new RuntimeException("Database error");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertTrue(body.getMessage().contains("unexpected") || body.getMessage().contains("error"));
    }
    
    @Test
    @DisplayName("handleGenericException_nullPointerException_returns500")
    void testHandleGenericException_nullPointerException_returns500() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null reference");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
    }
    
    @Test
    @DisplayName("handleGenericException_anyException_providesConsistentFormat")
    void testHandleGenericException_anyException_providesConsistentFormat() {
        // Arrange
        Exception exception = new Exception("Generic error");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);
        
        // Assert
        ErrorResponse body = response.getBody();
        assertNotNull(body.getTimestamp());
        assertNotNull(body.getStatus());
        assertNotNull(body.getError());
        assertNotNull(body.getMessage());
    }
    
    @Test
    @DisplayName("handleAccessDenied_multipleExceptions_eachReturns403")
    void testHandleAccessDenied_multipleExceptions_eachReturns403() {
        // Arrange
        AccessDeniedException ex1 = new AccessDeniedException("Access 1");
        AccessDeniedException ex2 = new AccessDeniedException("Access 2");
        
        // Act & Assert
        ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleAccessDenied(ex1);
        ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleAccessDenied(ex2);
        
        assertEquals(403, response1.getBody().getStatus());
        assertEquals(403, response2.getBody().getStatus());
    }
    
    @Test
    @DisplayName("handleAuthenticationError_differentExceptionMessages_maintainsStatus")
    void testHandleAuthenticationError_differentExceptionMessages_maintainsStatus() {
        // Arrange
        AuthenticationException ex1 = new AuthenticationException("Invalid token") {};
        AuthenticationException ex2 = new AuthenticationException("Token expired") {};
        
        // Act & Assert
        ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleAuthenticationError(ex1);
        ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleAuthenticationError(ex2);
        
        assertEquals(401, response1.getBody().getStatus());
        assertEquals(401, response2.getBody().getStatus());
    }
}
