package com.epam.executionengine.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request DTO for creating a shortened URL.
 * 
 * This DTO captures the client request to shorten a URL, including optional
 * parameters for custom short codes and expiration configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateShortUrlRequest {

    /**
     * The original long URL to be shortened.
     * Required, must be a valid URL format.
     */
    @NotBlank(message = "Long URL cannot be blank")
    private String longUrl;

    /**
     * Optional custom short code.
     * If provided, must be unique. If omitted, one will be auto-generated.
     */
    private String customCode;

    /**
     * Optional expiration period in days.
     * If null or 0, the URL never expires.
     * If positive, the URL will expire after the specified number of days.
     */
    @Positive(message = "Expiration days must be a positive number if provided")
    private Integer expirationDays;
}
