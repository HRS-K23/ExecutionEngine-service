package com.epam.executionengine.urlshortener.service;

import com.epam.executionengine.urlshortener.exception.ShortURLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * URL validation utility component.
 * 
 * Provides comprehensive URL validation including format, scheme, host presence,
 * and length constraints. Extracted from URLShorteningService to follow
 * Single Responsibility Principle and improve code reusability.
 * 
 * Uses java.net.URI (modern replacement for deprecated java.net.URL).
 */
@Component
@Slf4j
public class URLValidator {

    // RFC 3986 compliant URL length limit
    private static final int MAX_URL_LENGTH = 8000;
    
    // Allowed schemes for URLs
    private static final String ALLOWED_SCHEMES_PATTERN = "^(https?|ftp|ftps)$";
    private static final Pattern SCHEME_PATTERN = Pattern.compile(ALLOWED_SCHEMES_PATTERN, Pattern.CASE_INSENSITIVE);

    /**
     * Validates that the provided string is a valid URL.
     * 
     * Performs the following checks:
     * - URL is not null or blank
     * - URL does not exceed maximum length (8000 chars)
     * - URL has valid scheme (HTTP, HTTPS, FTP, FTPS)
     * - URL contains a valid host
     * 
     * @param urlString the URL string to validate
     * @throws ShortURLException if the URL is invalid
     */
    public void validateUrl(String urlString) {
        log.debug("Validating URL: {}", maskUrl(urlString));
        
        if (urlString == null || urlString.isBlank()) {
            log.warn("URL validation failed: URL is null or blank");
            throw new ShortURLException("URL cannot be null or empty");
        }

        if (urlString.length() > MAX_URL_LENGTH) {
            log.warn("URL validation failed: URL exceeds maximum length of {} characters", MAX_URL_LENGTH);
            throw new ShortURLException("URL exceeds maximum length of " + MAX_URL_LENGTH + " characters");
        }

        try {
            URI uri = URI.create(urlString);
            validateScheme(uri, urlString);
            validateHost(uri, urlString);
            
            log.debug("URL validation passed for: {}", maskUrl(urlString));
        } catch (IllegalArgumentException e) {
            log.warn("URL validation failed: Invalid URI format for {}", maskUrl(urlString), e);
            throw new ShortURLException("Invalid URL format: " + urlString);
        } catch (ShortURLException e) {
            throw e;
        }
    }

    /**
     * Validates that the URL has a valid HTTP/HTTPS scheme.
     * 
     * @param uri the parsed URI
     * @param originalUrl the original URL string (for error messages)
     * @throws ShortURLException if the scheme is invalid or missing
     */
    private void validateScheme(URI uri, String originalUrl) {
        String scheme = uri.getScheme();
        
        if (scheme == null || scheme.isBlank()) {
            log.warn("URL validation failed: No scheme provided in URL");
            throw new ShortURLException("URL must contain a valid scheme (HTTP/HTTPS)");
        }

        if (!SCHEME_PATTERN.matcher(scheme).matches()) {
            log.warn("URL validation failed: Invalid scheme '{}' (only HTTP/HTTPS/FTP/FTPS allowed)", scheme);
            throw new ShortURLException("URL must use HTTP/HTTPS/FTP/FTPS scheme, got: " + scheme);
        }
    }

    /**
     * Validates that the URL contains a valid host.
     * 
     * @param uri the parsed URI
     * @param originalUrl the original URL string (for error messages)
     * @throws ShortURLException if the host is missing or invalid
     */
    private void validateHost(URI uri, String originalUrl) {
        String host = uri.getHost();
        
        if (host == null || host.isBlank()) {
            log.warn("URL validation failed: No host provided in URL");
            throw new ShortURLException("URL must contain a valid host");
        }

        // Basic host validation: check for empty parts or invalid characters
        if (host.contains("..") || host.startsWith(".") || host.endsWith(".")) {
            log.warn("URL validation failed: Invalid host format: {}", host);
            throw new ShortURLException("Invalid host format: " + host);
        }
    }

    /**
     * Masks sensitive URL information in logs by showing only the scheme and host.
     * 
     * @param url the full URL
     * @return a masked version safe for logging
     */
    private String maskUrl(String url) {
        if (url == null || url.isBlank()) {
            return "<null>";
        }
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme() != null ? uri.getScheme() : "unknown";
            String host = uri.getHost() != null ? uri.getHost() : "unknown";
            return scheme + "://" + host + "/***";
        } catch (Exception e) {
            return "<invalid>";
        }
    }
}
