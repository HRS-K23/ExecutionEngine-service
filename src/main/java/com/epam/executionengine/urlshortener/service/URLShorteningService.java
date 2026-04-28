package com.epam.executionengine.urlshortener.service;

import com.epam.executionengine.urlshortener.dto.AnalyticsResponse;
import com.epam.executionengine.urlshortener.dto.CreateShortUrlRequest;
import com.epam.executionengine.urlshortener.dto.CreateShortUrlResponse;
import com.epam.executionengine.urlshortener.entity.ShortenedUrl;
import com.epam.executionengine.urlshortener.exception.URLExpiredException;
import com.epam.executionengine.urlshortener.exception.URLNotFoundException;
import com.epam.executionengine.urlshortener.exception.ShortURLException;
import com.epam.executionengine.urlshortener.repository.ShortenedUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for URL shortening operations.
 * 
 * Handles business logic including:
 * - Creating shortened URLs with auto-generated or custom short codes
 * - Retrieving shortened URLs by short code
 * - Handling URL expiration
 * - Tracking access counts
 * - Providing analytics
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class URLShorteningService {

    private final ShortenedUrlRepository repository;
    private final ShortCodeGenerator codeGenerator;

    @Value("${urlshortener.base-url:https://exe.local/s}")
    private String baseUrl;

    @Value("${urlshortener.short-code-length:6}")
    private int shortCodeLength;

    @Value("${urlshortener.collision-retry-limit:5}")
    private int collisionRetryLimit;

    /**
     * Creates a shortened URL from the provided request.
     * 
     * Handles duplicate URLs by returning the existing shortened URL if one
     * already exists for the same long URL. Generates unique short codes with
     * collision detection and retry logic.
     * 
     * @param request the request containing the long URL and optional parameters
     * @param createdBy identifier of the user/service creating the short URL
     * @return the created shortened URL information
     * @throws ShortURLException if the URL is invalid or max retries exceeded
     */
    public CreateShortUrlResponse createShortUrl(CreateShortUrlRequest request, String createdBy) {
        log.info("Creating shortened URL for: {}", request.getLongUrl());

        // Validate URL format
        validateUrl(request.getLongUrl());

        // Check for duplicate: return existing if URL already shortened
        var existingUrl = repository.findByLongUrl(request.getLongUrl());
        if (existingUrl.isPresent()) {
            log.info("Shortened URL already exists for long URL: {}", request.getLongUrl());
            return mapToResponse(existingUrl.get());
        }

        // Determine short code: use custom if provided, otherwise generate
        String shortCode;
        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            shortCode = request.getCustomCode();
            // Validate custom code is not already taken
            if (repository.existsByShortCode(shortCode)) {
                log.warn("Custom short code already exists: {}", shortCode);
                throw new ShortURLException("Custom short code '" + shortCode + "' is already taken");
            }
        } else {
            // Generate auto short code with collision handling
            shortCode = generateUniqueShortCode(request.getLongUrl());
        }

        // Calculate expiration date if specified
        LocalDateTime expiresAt = null;
        if (request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpirationDays());
        }

        // Create and persist the shortened URL
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .shortCode(shortCode)
                .longUrl(request.getLongUrl())
                .expiresAt(expiresAt)
                .createdBy(createdBy)
                .accessCount(0L)
                .build();

        ShortenedUrl saved = repository.save(shortenedUrl);
        log.info("Shortened URL created: {} -> {}", shortCode, request.getLongUrl());

        return mapToResponse(saved);
    }

    /**
     * Retrieves the original URL for a given short code.
     * 
     * Increments the access count on successful retrieval.
     * Checks for expiration and returns appropriate error if expired.
     * 
     * @param shortCode the short code
     * @return the original long URL
     * @throws URLNotFoundException if short code not found
     * @throws URLExpiredException if the URL has expired
     */
    @Transactional
    public String getOriginalUrl(String shortCode) {
        log.debug("Retrieving original URL for short code: {}", shortCode);

        ShortenedUrl shortenedUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Short code not found: {}", shortCode);
                    return new URLNotFoundException("Short code '" + shortCode + "' not found");
                });

        // Check if expired
        if (shortenedUrl.isExpired()) {
            log.warn("Short code has expired: {}", shortCode);
            throw new URLExpiredException("Short URL has expired");
        }

        // Increment access count
        shortenedUrl.setAccessCount(shortenedUrl.getAccessCount() + 1);
        repository.save(shortenedUrl);

        log.info("Redirecting short code: {} to {}", shortCode, shortenedUrl.getLongUrl());
        return shortenedUrl.getLongUrl();
    }

    /**
     * Retrieves analytics for a shortened URL.
     * 
     * @param shortCode the short code
     * @return analytics information including access count and breakdown
     * @throws URLNotFoundException if short code not found
     */
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(String shortCode) {
        log.debug("Retrieving analytics for short code: {}", shortCode);

        ShortenedUrl shortenedUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new URLNotFoundException("Short code '" + shortCode + "' not found"));

        // For this MVP, we return basic analytics. In production, this would query
        // a separate access_log table for detailed daily breakdown.
        List<AnalyticsResponse.DailyAccessCount> dailyBreakdown = new ArrayList<>();
        if (shortenedUrl.getAccessCount() > 0) {
            // Simplified: assume all accesses were today for MVP
            dailyBreakdown.add(AnalyticsResponse.DailyAccessCount.builder()
                    .date(shortenedUrl.getCreatedAt().toLocalDate())
                    .count(shortenedUrl.getAccessCount())
                    .build());
        }

        return AnalyticsResponse.builder()
                .shortCode(shortenedUrl.getShortCode())
                .shortUrl(buildFullShortUrl(shortenedUrl.getShortCode()))
                .longUrl(shortenedUrl.getLongUrl())
                .createdAt(shortenedUrl.getCreatedAt())
                .expiresAt(shortenedUrl.getExpiresAt())
                .totalAccessCount(shortenedUrl.getAccessCount())
                .accessCountByDay(dailyBreakdown)
                .build();
    }

    /**
     * Generates a unique short code with collision detection and retry logic.
     * 
     * Attempts to generate a code up to the collision retry limit.
     * If a collision is detected, increments the attempt counter and retries.
     * 
     * @param url the URL to generate a short code for
     * @return a unique short code
     * @throws ShortURLException if unable to generate unique code after max retries
     */
    private String generateUniqueShortCode(String url) {
        for (int attempt = 0; attempt < collisionRetryLimit; attempt++) {
            String shortCode = codeGenerator.generateShortCode(url, attempt, shortCodeLength);

            if (!repository.existsByShortCode(shortCode)) {
                return shortCode; // Found unique code
            }

            log.debug("Collision detected for short code: {}, retrying (attempt: {})", shortCode, attempt + 1);
        }

        log.error("Unable to generate unique short code after {} attempts for URL: {}", collisionRetryLimit, url);
        throw new ShortURLException("Unable to generate unique short code after " + collisionRetryLimit + " attempts");
    }

    /**
     * Validates that the provided string is a valid URL.
     * 
     * @param urlString the URL string to validate
     * @throws ShortURLException if the URL is invalid
     */
    private void validateUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new ShortURLException("URL cannot be null or empty");
        }

        try {
            // Attempt to parse as URL to validate format
            new URL(urlString);
        } catch (Exception e) {
            log.warn("Invalid URL format: {}", urlString, e);
            throw new ShortURLException("Invalid URL format: " + urlString);
        }

        // Check URL length
        if (urlString.length() > 8000) {
            throw new ShortURLException("URL exceeds maximum length of 8000 characters");
        }
    }

    /**
     * Builds the full short URL from a short code.
     * 
     * @param shortCode the short code
     * @return the complete short URL (e.g., "https://exe.local/s/aB3xY9")
     */
    private String buildFullShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }

    /**
     * Maps a ShortenedUrl entity to a CreateShortUrlResponse DTO.
     * 
     * @param entity the entity to map
     * @return the response DTO
     */
    private CreateShortUrlResponse mapToResponse(ShortenedUrl entity) {
        return CreateShortUrlResponse.builder()
                .id(entity.getId())
                .shortCode(entity.getShortCode())
                .shortUrl(buildFullShortUrl(entity.getShortCode()))
                .longUrl(entity.getLongUrl())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .accessCount(entity.getAccessCount())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    /**
     * Retrieves a shortened URL by its ID.
     * 
     * @param id the UUID of the shortened URL
     * @return the response DTO
     * @throws URLNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public CreateShortUrlResponse getShortenedUrlById(UUID id) {
        ShortenedUrl shortenedUrl = repository.findById(id)
                .orElseThrow(() -> new URLNotFoundException("Shortened URL not found with ID: " + id));

        return mapToResponse(shortenedUrl);
    }
}
