package com.epam.executionengine.urlshortener.exception;

/**
 * Base exception class for URL shortening service errors.
 * 
 * All exceptions thrown by the URL shortening service inherit from this class,
 * allowing for unified exception handling at the controller and global levels.
 */
public class ShortURLException extends RuntimeException {

    public ShortURLException(String message) {
        super(message);
    }

    public ShortURLException(String message, Throwable cause) {
        super(message, cause);
    }
}
