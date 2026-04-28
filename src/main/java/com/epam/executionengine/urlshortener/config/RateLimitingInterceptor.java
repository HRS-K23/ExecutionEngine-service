package com.epam.executionengine.urlshortener.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Interceptor for implementing rate limiting on URL shortening endpoints.
 * 
 * CRITICAL: Rate Limiting Configuration Integration (HIGH Priority Issue #7)
 * 
 * Implements per-IP rate limiting using Guava RateLimiter to prevent abuse and
 * ensure fair resource allocation. Allows a configured number of requests per
 * minute per IP address. Returns HTTP 429 (Too Many Requests) when limit exceeded.
 * 
 * Rate Limiting Strategy:
 * - Per-IP address based on X-Forwarded-For header (for proxied requests)
 * - Automatic cache expiry after 1 hour of inactivity
 * - Configurable requests per minute threshold
 * - Can be disabled via configuration
 * 
 * Configuration (application.properties):
 * - urlshortener.rate-limit.enabled: true/false (default: true)
 * - urlshortener.rate-limit.requests-per-minute: 100 (default)
 * 
 * NOTE: RateLimiters are cached per IP and automatically cleaned up after 1 hour
 * to prevent memory leak for requests from many different IPs.
 */
@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    @Value("${urlshortener.rate-limit.enabled:true}")
    private boolean rateLimitingEnabled;

    @Value("${urlshortener.rate-limit.requests-per-minute:" + URLShorteningConstants.DEFAULT_REQUESTS_PER_MINUTE + "}")
    private double requestsPerMinute;

    /**
     * Cache of RateLimiters, one per IP address.
     * 
     * Automatically removes entries after 1 hour of inactivity to prevent
     * memory accumulation from requests from many different IPs.
     */
    private final LoadingCache<String, RateLimiter> rateLimiters = CacheBuilder.newBuilder()
            .expireAfterAccess(URLShorteningConstants.RATE_LIMITER_CACHE_EXPIRY_HOURS, TimeUnit.HOURS)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) {
                    // Convert requests per minute to requests per second
                    double requestsPerSecond = requestsPerMinute / 60.0;
                    log.debug("Creating new RateLimiter for IP {} with {} req/sec", key, requestsPerSecond);
                    return RateLimiter.create(requestsPerSecond);
                }
            });

    /**
     * Pre-handle method for rate limiting check.
     * 
     * Extracts client IP address, checks rate limit, and either allows or blocks
     * the request based on whether the token bucket has capacity.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler method
     * @return true to continue processing, false to block the request
     * @throws IOException if response writing fails
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        
        if (!rateLimitingEnabled) {
            log.debug("Rate limiting disabled, allowing request");
            return true; // Rate limiting disabled, allow request
        }

        String clientIp = getClientIpAddress(request);
        RateLimiter rateLimiter = getRateLimiter(clientIp);

        if (!rateLimiter.tryAcquire()) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {} - max: {} req/min",
                    clientIp, request.getRequestURI(), (int) requestsPerMinute);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(buildErrorResponse());
            return false; // Block the request
        }

        log.debug("Rate limit check passed for IP: {} - remaining requests available", clientIp);
        return true; // Allow request to proceed
    }

    /**
     * Builds a JSON error response for rate limit exceeded.
     * 
     * @return JSON response body
     */
    private String buildErrorResponse() {
        return String.format(
                "{\"error\": \"Rate limit exceeded\", \"message\": \"Maximum %d requests per minute allowed\", \"retryAfter\": 60}",
                (int) requestsPerMinute
        );
    }

    /**
     * Extracts the client IP address from the request, considering X-Forwarded-For header.
     * 
     * This is important for requests coming through proxies or load balancers, which
     * append the original client IP to the X-Forwarded-For header.
     * 
     * Fallback order:
     * 1. X-Forwarded-For header (first IP for multi-hop scenarios)
     * 2. X-Forwarded-Host header
     * 3. request.getRemoteAddr()
     * 
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(URLShorteningConstants.HEADER_X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs (proxy chain); use the first one
            String clientIp = xForwardedFor.split(",")[0].trim();
            log.debug("Client IP extracted from X-Forwarded-For: {}", clientIp);
            return clientIp;
        }
        
        String xForwardedHost = request.getHeader(URLShorteningConstants.HEADER_X_FORWARDED_HOST);
        if (xForwardedHost != null && !xForwardedHost.isEmpty()) {
            log.debug("Client IP extracted from X-Forwarded-Host: {}", xForwardedHost);
            return xForwardedHost;
        }
        
        String clientIp = request.getRemoteAddr();
        log.debug("Client IP extracted from remoteAddr: {}", clientIp);
        return clientIp;
    }

    /**
     * Gets or creates a RateLimiter for the given IP address.
     * 
     * Uses LoadingCache to automatically create new RateLimiters for new IPs and
     * automatically evict old entries after 1 hour of inactivity.
     * 
     * @param clientIp the client IP address
     * @return the RateLimiter for this IP
     */
    private RateLimiter getRateLimiter(String clientIp) {
        try {
            return rateLimiters.get(clientIp);
        } catch (ExecutionException e) {
            log.error("Error loading rate limiter for IP: {}", clientIp, e);
            // Create a temporary rate limiter as fallback
            double requestsPerSecond = requestsPerMinute / 60.0;
            log.warn("Returning fallback rate limiter for IP: {}", clientIp);
            return RateLimiter.create(requestsPerSecond);
        }
    }
}
