package com.epam.executionengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for application.
 * Sets up authentication, authorization, and security filters.
 * 
 * Development mode:
 * - In-memory user store with test users
 * - H2 console permitted for database access
 * - CSRF disabled for stateless API
 * 
 * @author EPMICMPCOD-300
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    
    /**
     * Configure security filter chain.
     * 
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/v1/tasks/**").authenticated()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {})
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        
        return http.build();
    }
    
    /**
     * Configure in-memory user details service for development.
     * 
     * Users:
     * - user1 / password123 (role: USER)
     * - user2 / password456 (role: USER)
     * 
     * @return the UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user1 = User.builder()
            .username("user1")
            .password(passwordEncoder().encode("password123"))
            .roles("USER")
            .build();
        
        UserDetails user2 = User.builder()
            .username("user2")
            .password(passwordEncoder().encode("password456"))
            .roles("USER")
            .build();
        
        return new InMemoryUserDetailsManager(user1, user2);
    }
    
    /**
     * Configure password encoder.
     * Uses BCrypt with salt and iterations for secure password hashing.
     * 
     * @return the PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
