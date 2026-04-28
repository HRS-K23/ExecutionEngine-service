package com.epam.executionengine.urlshortener.config;

/**
 * Configuration constants for URL shortening service.
 * 
 * Centralizes magic values and configuration parameters to improve
 * maintainability and follow DRY (Don't Repeat Yourself) principle.
 */
public class URLShorteningConstants {

    // URL Validation Constants
    public static final int MAX_URL_LENGTH = 8000;
    public static final int MIN_URL_LENGTH = 7;  // Minimum: "http://x"
    public static final String[] ALLOWED_SCHEMES = {"http", "https", "ftp", "ftps"};

    // Short Code Generation Constants
    public static final int DEFAULT_SHORT_CODE_LENGTH = 6;
    public static final int MIN_SHORT_CODE_LENGTH = 4;
    public static final int MAX_SHORT_CODE_LENGTH = 20;
    public static final int DEFAULT_COLLISION_RETRY_LIMIT = 5;
    public static final int MAX_COLLISION_RETRY_LIMIT = 10;

    // Exponential Backoff Constants for Collision Retry
    public static final long BACKOFF_INITIAL_MS = 100;      // 100ms initial backoff
    public static final long BACKOFF_MAX_MS = 5000;         // 5 second max backoff
    public static final double BACKOFF_MULTIPLIER = 2.0;    // Double each retry

    // URL Expiration Constants
    public static final int MIN_EXPIRATION_DAYS = 1;
    public static final int MAX_EXPIRATION_DAYS = 3650;     // ~10 years

    // Rate Limiting Constants
    public static final int DEFAULT_REQUESTS_PER_MINUTE = 100;
    public static final int RATE_LIMITER_CACHE_EXPIRY_HOURS = 1;

    // CORS Constants
    public static final String DEFAULT_CORS_ALLOWED_ORIGINS = "*";
    public static final long DEFAULT_CORS_MAX_AGE_SECONDS = 3600;

    // HTTP Constants
    public static final String API_BASE_PATH = "/api/v1/urls";
    public static final String DEFAULT_BASE_URL = "https://exe.local/s";
    public static final String REDIRECT_PATH_PATTERN = "/api/v1/urls/**";
    public static final String HEALTH_CHECK_PATH = "/api/v1/urls/health";

    // Logging Constants
    public static final String LOG_URL_PATTERN_TEMPLATE = "{}";
    public static final String LOG_MASKED_URL_PATTERN = "scheme://host/***";

    // Response Constants
    public static final String ERROR_URL_NOT_FOUND = "Short code '%s' not found";
    public static final String ERROR_URL_EXPIRED = "Short URL has expired";
    public static final String ERROR_INVALID_URL_FORMAT = "Invalid URL format: %s";
    public static final String ERROR_CUSTOM_CODE_TAKEN = "Custom short code '%s' is already taken";
    public static final String ERROR_CANNOT_GENERATE_CODE = "Unable to generate unique short code after %d attempts";
    public static final String ERROR_DUPLICATE_FOUND = "Shortened URL already exists for long URL: %s";

    // Custom Headers
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";
    public static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";
    public static final String HEADER_X_PAGE_NUMBER = "X-Page-Number";

    private URLShorteningConstants() {
        // Prevent instantiation of constants class
        throw new AssertionError("Utility class should not be instantiated");
    }
}
