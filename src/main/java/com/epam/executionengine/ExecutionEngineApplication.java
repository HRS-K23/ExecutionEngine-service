package com.epam.executionengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * ExecutionEngine Service Application - Main Entry Point
 * 
 * This Spring Boot application includes the URL Shortening Microservice
 * as an integrated module within the ExecutionEngine platform.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.epam.executionengine"
})
public class ExecutionEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExecutionEngineApplication.class, args);
    }
}
