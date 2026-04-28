package com.epam.executionengine.urlshortener.exception;

/**
 * Exception thrown when a requested shortened URL has expired.
 * 
 * Maps to HTTP 410 (Gone) response, indicating that the resource is no longer available.
 */
public class URLExpiredException extends ShortURLException {

    public URLExpiredException(String message) {
        super(message);
    }

    public URLExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
