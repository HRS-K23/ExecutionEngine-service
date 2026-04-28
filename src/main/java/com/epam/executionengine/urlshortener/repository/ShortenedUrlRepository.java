package com.epam.executionengine.urlshortener.repository;

import com.epam.executionengine.urlshortener.entity.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Atomically increments the access count for a shortened URL by 1.
     * 
     * This is performed at the database level to prevent lost updates under
     * concurrent load. Ensures that all accesses are counted accurately.
     * 
     * Fix for CRITICAL Issue #2: Race Condition in Access Count Increment
     * 
     * @param shortCode the short code to increment
     */
    @Modifying
    @Query("UPDATE ShortenedUrl SET accessCount = accessCount + 1 WHERE shortCode = :shortCode")
    void incrementAccessCount(@Param("shortCode") String shortCode);
}
