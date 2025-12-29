package com.canvify.test.security;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@#$%&*!";

    private static final String ALL = LOWER + UPPER + DIGITS + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a secure random password
     * @param length recommended >= 10
     */
    public static String generate(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8");
        }

        StringBuilder password = new StringBuilder(length);

        // Ensure minimum complexity
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        // Fill remaining characters
        for (int i = 4; i < length; i++) {
            password.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        // Shuffle to remove predictable order
        return shuffle(password.toString());
    }

    private static String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}

