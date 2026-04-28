package com.epam.executionengine.urlshortener.repository;

import com.epam.executionengine.urlshortener.entity.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for ShortenedUrl entity.
 * 
 * Provides CRUD operations and custom query methods for managing shortened URLs
 * in the database.
 */
@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, UUID> {

    /**
     * Find a shortened URL by its short code.
     * 
     * @param shortCode the short code (e.g., "aB3xY9")
     * @return Optional containing the ShortenedUrl if found, empty otherwise
     */
    Optional<ShortenedUrl> findByShortCode(String shortCode);

    /**
     * Find a shortened URL by its original long URL.
     * 
     * Useful for detecting duplicates and preventing redundant entries
     * for URLs that have already been shortened.
     * 
     * @param longUrl the original long URL
     * @return Optional containing the ShortenedUrl if found, empty otherwise
     */
    Optional<ShortenedUrl> findByLongUrl(String longUrl);

    /**
     * Check if a short code already exists in the database.
     * 
     * @param shortCode the short code to check
     * @return true if the short code exists, false otherwise
     */
    boolean existsByShortCode(String shortCode);
}
