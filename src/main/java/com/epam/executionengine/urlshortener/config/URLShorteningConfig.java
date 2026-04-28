package com.epam.executionengine.urlshortener.config;

import com.epam.executionengine.urlshortener.config.RateLimitingInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuration class for the URL shortening service.
 * 
 * Provides Spring Bean configurations for:
 * - OpenAPI/Swagger documentation
 * - CORS (Cross-Origin Resource Sharing) settings
 * - Rate limiting via HTTP interceptor
 * - Security and performance policies
 * 
 * Configuration properties (with defaults):
 * - urlshortener.cors.allowed-origins: "*" (development), restrict for production
 * - urlshortener.cors.max-age: 3600 seconds
 * - urlshortener.rate-limit.enabled: true
 * - urlshortener.rate-limit.requests-per-minute: 100
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class URLShorteningConfig {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Value("${urlshortener.cors.allowed-origins:" + URLShorteningConstants.DEFAULT_CORS_ALLOWED_ORIGINS + "}")
    private String allowedOrigins;

    @Value("${urlshortener.cors.max-age:" + URLShorteningConstants.DEFAULT_CORS_MAX_AGE_SECONDS + "}")
    private long corsMaxAge;

    /**
     * Configures OpenAPI documentation for Swagger/Springdoc.
     * 
     * Provides metadata and server information for API documentation,
     * enabling clients to discover and understand the API endpoints.
     * 
     * @return OpenAPI configuration with service metadata
     */
    @Bean
    public OpenAPI customOpenAPI() {
        log.debug("Configuring OpenAPI documentation");
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortening Service API")
                        .version("1.0.0")
                        .description("ExecutionEngine URL Shortening Microservice - Convert long URLs into shareable short URLs")
                        .contact(new Contact()
                                .name("EPAM ExecutionEngine Team")
                                .email("executionengine@epam.com")))
                .servers(List.of(
                        new Server().url("https://exe.local").description("Production Environment"),
                        new Server().url("http://localhost:8080").description("Local Development")
                ));
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) to allow API access from different origins.
     * 
     * CRITICAL: CORS Configuration (HIGH Priority Issue #6)
     * Enables cross-origin requests for frontend applications on different domains to access the API.
     * 
     * Configuration via application properties:
     * - urlshortener.cors.allowed-origins: Pattern for allowed origins (default: "*")
     * - urlshortener.cors.max-age: Preflight cache duration in seconds (default: 3600)
     * 
     * WARNING: For production, restrict allowed-origins to specific domains instead of "*"
     * 
     * Configuration:
     * - Mapped to: /api/v1/urls/** endpoints
     * - Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
     * - Allowed Headers: All (*)
     * - Credentials: Allowed only for specific origins (not "*")
     * - Exposed Headers: X-Total-Count, X-Page-Number, Content-Type
     * 
     * @return WebMvcConfigurer with CORS mapping and interceptor registration
     */
    @Bean
    public WebMvcConfigurer corsAndInterceptorConfigurer() {
        log.info("Configuring CORS with allowed origins: {}", allowedOrigins);
        
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Configure CORS for main API endpoints
                registry.addMapping(URLShorteningConstants.REDIRECT_PATH_PATTERN)
                        .allowedOriginPatterns(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials("*".equals(allowedOrigins) ? false : true)
                        .maxAge(corsMaxAge)
                        .exposedHeaders(
                                URLShorteningConstants.HEADER_X_TOTAL_COUNT,
                                URLShorteningConstants.HEADER_X_PAGE_NUMBER,
                                "Content-Type"
                        );
                
                log.debug("CORS configured for {} with max-age: {} seconds", 
                        URLShorteningConstants.REDIRECT_PATH_PATTERN, corsMaxAge);
                
                // Also allow health check endpoint from all origins
                registry.addMapping(URLShorteningConstants.HEALTH_CHECK_PATH)
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "OPTIONS")
                        .maxAge(corsMaxAge);
                
                log.debug("CORS configured for health check endpoint");
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // Register rate limiting interceptor for URL shortening endpoints
                registry.addInterceptor(rateLimitingInterceptor)
                        .addPathPatterns(URLShorteningConstants.REDIRECT_PATH_PATTERN)
                        .excludePathPatterns(URLShorteningConstants.HEALTH_CHECK_PATH);
                
                log.debug("Rate limiting interceptor registered for {} endpoints", 
                        URLShorteningConstants.REDIRECT_PATH_PATTERN);
            }
        };
    }
}
