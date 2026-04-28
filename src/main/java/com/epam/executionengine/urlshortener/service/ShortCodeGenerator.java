package com.epam.executionengine.urlshortener.service;

import com.epam.executionengine.urlshortener.config.URLShorteningConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility component for generating short codes using Base62 encoding.
 * 
 * Converts hash values to compact, URL-safe Base62 strings (0-9, A-Z, a-z)
 * that serve as unique identifiers for shortened URLs.
 * 
 * Base62 is used because it:
 * - Is URL-safe (no special characters)
 * - Is case-sensitive (maximizes uniqueness in limited space)
 * - Is efficient (62 characters per position vs 16 for hex, 2 for binary)
 * 
 * Character Set:
 * - Digits: 0-9 (10 characters)
 * - Uppercase: A-Z (26 characters)
 * - Lowercase: a-z (26 characters)
 * - Total: 62 characters per position
 */
@Component
@Slf4j
public class ShortCodeGenerator {

    /**
     * Base62 character set: 0-9 (10), A-Z (26), a-z (26) = 62 total characters.
     */
    private static final String BASE62_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    /**
     * Encodes a long number (hash) into a Base62 string.
     * 
     * Algorithm:
     * 1. Convert absolute value to base 62
     * 2. For each digit, look up character from charset
     * 3. Reverse the result for correct ordering
     * 
     * @param number the number to encode (typically a hash value)
     * @return the Base62 encoded string
     */
    public String encode(long number) {
        if (number == 0) {
            return String.valueOf(BASE62_CHARSET.charAt(0));
        }

        StringBuilder result = new StringBuilder();
        long absNumber = Math.abs(number);

        while (absNumber > 0) {
            result.append(BASE62_CHARSET.charAt((int) (absNumber % BASE)));
            absNumber /= BASE;
        }

        return result.reverse().toString();
    }

    /**
     * Decodes a Base62 string back into a long number.
     * 
     * Algorithm:
     * 1. For each character in the Base62 string
     * 2. Find its position in the charset (0-61)
     * 3. Multiply accumulated result by base (62) and add digit value
     * 
     * @param encoded the Base62 encoded string
     * @return the decoded long value
     * @throws IllegalArgumentException if the string contains invalid Base62 characters
     */
    public long decode(String encoded) {
        long result = 0;

        for (char character : encoded.toCharArray()) {
            int digitValue = BASE62_CHARSET.indexOf(character);
            if (digitValue < 0) {
                log.warn("Invalid Base62 character encountered: {} in code: {}", character, encoded);
                throw new IllegalArgumentException("Invalid Base62 character: " + character);
            }
            result = result * BASE + digitValue;
        }

        return result;
    }

    /**
     * Generates a short code from a given URL using a sequential counter.
     * 
     * Strategy:
     * - Combines URL hash with attempt number for uniqueness
     * - On retry (collision), attempt number increments, creating different codes
     * - Pads or truncates to exact requested length
     * 
     * Collision Recovery:
     * - If code is too short: pad left with '0' characters
     * - If code is too long: truncate to rightmost N characters (preserves variation)
     * 
     * @param url the URL to generate a short code for
     * @param attempt the attempt number (0 for first attempt, incremented on collision)
     * @param length the desired length of the short code
     * @return a Base62 encoded short code of the specified length
     */
    public String generateShortCode(String url, int attempt, int length) {
        // Combine URL hash with attempt number for uniqueness on retry
        long combinedHash = (long) url.hashCode() + attempt;
        String encoded = encode(combinedHash);
        
        log.debug("Generated encoded value: {} for URL hash + attempt: {}", encoded, attempt);
        
        // Adjust length: pad if too short, truncate if too long
        if (encoded.length() < length) {
            encoded = padWithLeadingZeros(encoded, length);
            log.debug("Padded code to length {}: {}", length, encoded);
        } else if (encoded.length() > length) {
            // Truncate from right to preserve more variation
            encoded = encoded.substring(encoded.length() - length);
            log.debug("Truncated code to length {}: {}", length, encoded);
        }
        
        return encoded;
    }

    /**
     * Pads a Base62 string with leading zeros to reach the desired length.
     * 
     * Uses the first character of the charset ('0') for padding to maintain
     * Base62 alphabet consistency.
     * 
     * @param value the string to pad
     * @param length the desired length
     * @return the padded string
     */
    private String padWithLeadingZeros(String value, int length) {
        while (value.length() < length) {
            value = BASE62_CHARSET.charAt(0) + value;
        }
        return value;
    }

    /**
     * Validates that a string is a valid Base62 encoded value.
     * 
     * Useful for validating user-provided custom short codes.
     * 
     * @param code the code to validate
     * @return true if all characters are valid Base62 characters, false otherwise
     */
    public boolean isValidBase62(String code) {
        if (code == null || code.isEmpty()) {
            log.debug("Code validation failed: null or empty");
            return false;
        }
        for (char c : code.toCharArray()) {
            if (BASE62_CHARSET.indexOf(c) < 0) {
                log.debug("Code validation failed: invalid character '{}' in code: {}", c, code);
                return false;
            }
        }
        log.debug("Code validation passed: {}", code);
        return true;
    }
}
