package com.epam.executionengine.urlshortener.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility component for generating short codes using Base62 encoding.
 * 
 * Converts hash values to compact, URL-safe Base62 strings (0-9, A-Z, a-z)
 * that serve as unique identifiers for shortened URLs.
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
     * @param encoded the Base62 encoded string
     * @return the decoded long value
     * @throws IllegalArgumentException if the string contains invalid Base62 characters
     */
    public long decode(String encoded) {
        long result = 0;

        for (char character : encoded.toCharArray()) {
            int digitValue = BASE62_CHARSET.indexOf(character);
            if (digitValue < 0) {
                throw new IllegalArgumentException("Invalid Base62 character: " + character);
            }
            result = result * BASE + digitValue;
        }

        return result;
    }

    /**
     * Generates a short code from a given URL using a sequential counter.
     * 
     * This method combines the URL's hash with an attempt counter to generate
     * unique short codes even for identical URLs on retry.
     * 
     * @param url the URL to generate a short code for
     * @param attempt the attempt number (0 for first attempt, incremented on collision)
     * @param length the desired length of the short code
     * @return a Base62 encoded short code of the specified length
     */
    public String generateShortCode(String url, int attempt, int length) {
        // Combine URL hash with attempt number for uniqueness
        long combinedHash = (long) url.hashCode() + attempt;
        String encoded = encode(combinedHash);
        
        // Pad with leading character if too short, or truncate if too long
        if (encoded.length() < length) {
            encoded = padWithLeadingZeros(encoded, length);
        } else if (encoded.length() > length) {
            encoded = encoded.substring(encoded.length() - length);
        }
        
        log.debug("Generated short code: {} for URL: {} (attempt: {})", encoded, url, attempt);
        return encoded;
    }

    /**
     * Pads a Base62 string with leading zeros to reach the desired length.
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
     * @param code the code to validate
     * @return true if all characters are valid Base62 characters, false otherwise
     */
    public boolean isValidBase62(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        for (char c : code.toCharArray()) {
            if (BASE62_CHARSET.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }
}
