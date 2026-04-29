package com.epam.executionengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Execution Engine Service application.
 *
 * This Spring Boot application provides REST APIs for managing execution tasks.
 * Configuration is auto-configured by Spring Boot based on classpath dependencies
 * and application.properties settings.
 *
 * @since 1.0.0
 */
@SpringBootApplication
public class ExecutionEngineApplication {

    /**
     * Application entry point.
     *
     * @param args Command-line arguments (optional)
     */
    public static void main(String[] args) {
        SpringApplication.run(ExecutionEngineApplication.class, args);
    }
}
