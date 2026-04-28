package com.epam.executionengine.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.epam.executionengine.urlshortener.config.URLShorteningConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for creating a shortened URL.
 * 
 * This DTO captures the client request to shorten a URL, including optional
 * parameters for custom short codes and expiration configuration.
 * 
 * Validation rules:
 * - Long URL: required, not blank, maximum 8000 characters
 * - Custom code: optional, must contain only alphanumeric characters, dashes, and underscores
 * - Expiration: optional, must be positive number of days if provided
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateShortUrlRequest {

    /**
     * The original long URL to be shortened.
     * 
     * Required, must be a valid URL format and not exceed 8000 characters.
     * Minimum length: 7 characters (e.g., "http://x")
     */
    @NotBlank(message = "Long URL cannot be blank")
    @Size(
        min = URLShorteningConstants.MIN_URL_LENGTH,
        max = URLShorteningConstants.MAX_URL_LENGTH,
        message = "URL must be between " + URLShorteningConstants.MIN_URL_LENGTH + " and " 
                  + URLShorteningConstants.MAX_URL_LENGTH + " characters"
    )
    private String longUrl;

    /**
     * Optional custom short code.
     * 
     * If provided, must be unique and contain only alphanumeric characters, dashes, and underscores.
     * If omitted, one will be auto-generated.
     * 
     * Length constraints: must be between 4-20 characters
     */
    @Size(
        min = URLShorteningConstants.MIN_SHORT_CODE_LENGTH,
        max = URLShorteningConstants.MAX_SHORT_CODE_LENGTH,
        message = "Custom code must be between " + URLShorteningConstants.MIN_SHORT_CODE_LENGTH 
                  + " and " + URLShorteningConstants.MAX_SHORT_CODE_LENGTH + " characters"
    )
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Custom code must contain only alphanumeric characters, dashes, and underscores"
    )
    private String customCode;

    /**
     * Optional expiration period in days.
     * 
     * If null or 0, the URL never expires.
     * If positive, the URL will expire after the specified number of days.
     * Maximum: 3650 days (~10 years)
     * Minimum: 1 day (if specified)
     */
    @Positive(message = "Expiration days must be a positive number if provided")
    private Integer expirationDays;
}
