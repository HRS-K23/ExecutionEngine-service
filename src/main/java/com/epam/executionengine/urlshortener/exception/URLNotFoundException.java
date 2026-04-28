package com.epam.executionengine.urlshortener.exception;

/**
 * Exception thrown when a requested shortened URL is not found in the database.
 * 
 * Maps to HTTP 404 (Not Found) response.
 */
public class URLNotFoundException extends ShortURLException {

    public URLNotFoundException(String message) {
        super(message);
    }

    public URLNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
