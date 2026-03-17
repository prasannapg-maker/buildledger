package com.buildledger.iam.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS    = "23456789";
    private static final String SPECIAL   = "@#$%^&+=!";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

    /**
     * Generate a cryptographically random temporary password of the given length.
     * Guarantees at least one character from each character class.
     */
    public String generateTemporaryPassword(int length) {
        if (length < 8) length = 12;

        StringBuilder password = new StringBuilder();

        // Ensure at least one from each class
        password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the result to avoid predictable prefix
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }

        return new String(chars);
    }

    /**
     * Generate a URL-safe secure random token for password reset or email verification.
     */
    public String generateSecureToken(int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, RANDOM);
    }

    /**
     * Default temporary password with length 12.
     */
    public String generateTemporaryPassword() {
        return generateTemporaryPassword(12);
    }
}
