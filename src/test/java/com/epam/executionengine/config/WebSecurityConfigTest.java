package com.epam.executionengine.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WebSecurityConfig Spring Security configuration.
 * Verifies authentication, authorization, and security filter chain setup.
 */
@SpringBootTest
@DisplayName("WebSecurityConfig Tests")
class WebSecurityConfigTest {
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired(required = false)
    private SecurityFilterChain securityFilterChain;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    @DisplayName("userDetailsService_user1Exists_loadsSuccessfully")
    void testUserDetailsService_user1Exists_loadsSuccessfully() {
        // Act
        UserDetails user1 = userDetailsService.loadUserByUsername("user1");
        
        // Assert
        assertNotNull(user1);
        assertEquals("user1", user1.getUsername());
        assertTrue(user1.isEnabled());
        assertTrue(user1.isAccountNonExpired());
        assertTrue(user1.isAccountNonLocked());
    }
    
    @Test
    @DisplayName("userDetailsService_user2Exists_loadsSuccessfully")
    void testUserDetailsService_user2Exists_loadsSuccessfully() {
        // Act
        UserDetails user2 = userDetailsService.loadUserByUsername("user2");
        
        // Assert
        assertNotNull(user2);
        assertEquals("user2", user2.getUsername());
        assertTrue(user2.isEnabled());
    }
    
    @Test
    @DisplayName("userDetailsService_user1HasUserRole_verified")
    void testUserDetailsService_user1HasUserRole_verified() {
        // Act
        UserDetails user1 = userDetailsService.loadUserByUsername("user1");
        
        // Assert
        assertNotNull(user1.getAuthorities());
        assertTrue(user1.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().contains("USER")));
    }
    
    @Test
    @DisplayName("userDetailsService_user2HasUserRole_verified")
    void testUserDetailsService_user2HasUserRole_verified() {
        // Act
        UserDetails user2 = userDetailsService.loadUserByUsername("user2");
        
        // Assert
        assertNotNull(user2.getAuthorities());
        assertTrue(user2.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().contains("USER")));
    }
    
    @Test
    @DisplayName("passwordEncoder_validateUser1Password_matchesEncoded")
    void testPasswordEncoder_validateUser1Password_matchesEncoded() {
        // Arrange
        UserDetails user1 = userDetailsService.loadUserByUsername("user1");
        
        // Act
        boolean matches = passwordEncoder.matches("password123", user1.getPassword());
        
        // Assert
        assertTrue(matches);
    }
    
    @Test
    @DisplayName("passwordEncoder_validateUser2Password_matchesEncoded")
    void testPasswordEncoder_validateUser2Password_matchesEncoded() {
        // Arrange
        UserDetails user2 = userDetailsService.loadUserByUsername("user2");
        
        // Act
        boolean matches = passwordEncoder.matches("password456", user2.getPassword());
        
        // Assert
        assertTrue(matches);
    }
    
    @Test
    @DisplayName("passwordEncoder_wrongPassword_doesNotMatch")
    void testPasswordEncoder_wrongPassword_doesNotMatch() {
        // Arrange
        UserDetails user1 = userDetailsService.loadUserByUsername("user1");
        
        // Act
        boolean matches = passwordEncoder.matches("wrongpassword", user1.getPassword());
        
        // Assert
        assertFalse(matches);
    }
    
    @Test
    @DisplayName("securityFilterChain_beanExists_configured")
    void testSecurityFilterChain_beanExists_configured() {
        // Assert
        assertNotNull(securityFilterChain);
    }
    
    @Test
    @DisplayName("userDetailsService_nonexistentUser_throwsException")
    void testUserDetailsService_nonexistentUser_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> 
            userDetailsService.loadUserByUsername("nonexistentuser")
        );
    }
    
    @Test
    @DisplayName("userDetailsService_emptyUsername_throwsException")
    void testUserDetailsService_emptyUsername_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> 
            userDetailsService.loadUserByUsername("")
        );
    }
    
    @Test
    @DisplayName("passwordEncoder_encodePassword_producesEncryptedValue")
    void testPasswordEncoder_encodePassword_producesEncryptedValue() {
        // Act
        String encoded = passwordEncoder.encode("testpassword");
        
        // Assert
        assertNotNull(encoded);
        assertNotEquals("testpassword", encoded);
        assertTrue(passwordEncoder.matches("testpassword", encoded));
    }
    
    @Test
    @DisplayName("passwordEncoder_samePlaintext_producesDifferentHashes")
    void testPasswordEncoder_samePlaintext_producesDifferentHashes() {
        // Act
        String hash1 = passwordEncoder.encode("samepassword");
        String hash2 = passwordEncoder.encode("samepassword");
        
        // Assert
        assertNotEquals(hash1, hash2); // BCrypt is salted, should produce different hashes
        assertTrue(passwordEncoder.matches("samepassword", hash1));
        assertTrue(passwordEncoder.matches("samepassword", hash2));
    }
}
