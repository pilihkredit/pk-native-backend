package com.pk.core.auth;

public final class PasswordFormatValidator {
    private PasswordFormatValidator() {
    }

    public static boolean isValid(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int index = 0; index < password.length(); index++) {
            char character = password.charAt(index);
            if (Character.isLetter(character)) {
                hasLetter = true;
            }
            if (Character.isDigit(character)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }
}
