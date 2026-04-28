package com.epam.executionengine.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for the URL shortening service.
 * 
 * Provides Spring Bean configurations for OpenAPI documentation and service settings.
 */
@Configuration
public class URLShorteningConfig {

    /**
     * Configures OpenAPI documentation for Swagger/Springdoc.
     * 
     * @return OpenAPI configuration with service metadata
     */
    @Bean
    public OpenAPI customOpenAPI() {
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
}
