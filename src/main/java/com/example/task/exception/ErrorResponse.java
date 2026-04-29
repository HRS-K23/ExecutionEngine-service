package com.example.task.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String error;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    
    public ErrorResponse() {
    }
    
    public ErrorResponse(String error, int status, LocalDateTime timestamp, String path) {
        this.error = error;
        this.status = status;
        this.timestamp = timestamp;
        this.path = path;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}
