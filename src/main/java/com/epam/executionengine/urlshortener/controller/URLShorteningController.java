package com.epam.executionengine.urlshortener.controller;

import com.epam.executionengine.urlshortener.dto.AnalyticsResponse;
import com.epam.executionengine.urlshortener.dto.CreateShortUrlRequest;
import com.epam.executionengine.urlshortener.dto.CreateShortUrlResponse;
import com.epam.executionengine.urlshortener.service.URLShorteningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST Controller for URL shortening operations.
 * 
 * Provides endpoints for:
 * - Creating shortened URLs
 * - Redirecting to original URLs
 * - Retrieving analytics for shortened URLs
 */
@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "URL Shortening", description = "API for creating and retrieving shortened URLs")
public class URLShorteningController {

    private final URLShorteningService service;

    /**
     * Creates a shortened URL from a long URL.
     * 
     * Endpoint: POST /api/v1/urls/shorten
     * 
     * @param request contains the long URL and optional parameters
     * @return 201 Created with the created shortened URL information
     */
    @PostMapping("/shorten")
    @Operation(summary = "Create a shortened URL", description = "Converts a long URL into a shareable short URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Short URL successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateShortUrlResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request: malformed URL, invalid code, or conflicting custom code"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CreateShortUrlResponse> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request) {

        log.info("Received request to shorten URL: {}", request.getLongUrl());

        // Use a default user identifier (in production, get from authentication context)
        String createdBy = "system-user";

        CreateShortUrlResponse response = service.createShortUrl(request, createdBy);

        log.info("Short URL created successfully: {}", response.getShortCode());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Redirects to the original URL for a given short code.
     * 
     * Endpoint: GET /api/v1/urls/{shortCode}
     * 
     * @param shortCode the short code
     * @param response the HTTP response object for redirect
     * @throws IOException if redirect fails
     */
    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL", description = "Returns a redirect (301/302) to the original long URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "301", description = "Permanently moved to the original URL"),
            @ApiResponse(responseCode = "302", description = "Found - temporary redirect to the original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found"),
            @ApiResponse(responseCode = "410", description = "Short URL has expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void redirectToOriginalUrl(
            @PathVariable String shortCode,
            HttpServletResponse response) throws IOException {

        log.info("Redirect request for short code: {}", shortCode);

        String originalUrl = service.getOriginalUrl(shortCode);

        log.info("Redirecting {} to {}", shortCode, originalUrl);

        // Use HTTP 301 (Moved Permanently) for permanent redirects
        // In some use cases, HTTP 302 (Found) might be preferred for tracking purposes
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", originalUrl);
    }

    /**
     * Retrieves analytics for a shortened URL.
     * 
     * Endpoint: GET /api/v1/urls/{shortCode}/analytics
     * 
     * @param shortCode the short code
     * @return 200 OK with analytics information
     */
    @GetMapping("/{shortCode}/analytics")
    @Operation(summary = "Get URL analytics", description = "Returns access statistics for a shortened URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalyticsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Short code not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable String shortCode) {

        log.info("Analytics request for short code: {}", shortCode);

        AnalyticsResponse analytics = service.getAnalytics(shortCode);

        return ResponseEntity.ok(analytics);
    }

    /**
     * Health check endpoint for the URL shortening service.
     * 
     * @return 200 OK with status message
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifies that the URL shortening service is operational")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("URL Shortening Service is running");
    }
}
