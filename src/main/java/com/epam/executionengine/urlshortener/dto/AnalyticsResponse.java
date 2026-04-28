package com.epam.executionengine.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for URL analytics endpoint.
 * 
 * Provides aggregated access statistics for a shortened URL, including
 * total access count and breakdown by day.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsResponse {

    /**
     * The short code for this URL.
     */
    private String shortCode;

    /**
     * The full short URL.
     */
    private String shortUrl;

    /**
     * The original long URL.
     */
    private String longUrl;

    /**
     * Timestamp when the shortened URL was created.
     */
    private LocalDateTime createdAt;

    /**
     * Expiration timestamp, if set.
     */
    private LocalDateTime expiresAt;

    /**
     * Total number of times this URL has been accessed.
     */
    private Long totalAccessCount;

    /**
     * Breakdown of access count by day.
     * Each entry contains a date and the number of accesses on that date.
     */
    private List<DailyAccessCount> accessCountByDay;

    /**
     * Nested DTO for daily access count breakdown.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAccessCount {
        
        /**
         * The date for which this access count is reported.
         */
        private LocalDate date;
        
        /**
         * Number of accesses on this date.
         */
        private Long count;
    }
}
