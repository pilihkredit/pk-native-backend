package com.pk.infra.auth;

/**
 * Indonesian mobile number rules: pure digits, starts with {@code 8}, length 9–32.
 * Rejects country prefixes ({@code +86}, {@code 0}, {@code 62}) and embedded spaces.
 */
public final class MobileNumberValidator {
    private MobileNumberValidator() {
    }

    public static boolean isValid(String mobileNo) {
        if (mobileNo == null || mobileNo.isBlank()) {
            return false;
        }
        String normalized = mobileNo.trim();
        if (normalized.contains(" ")
                || normalized.startsWith("+86")
                || normalized.startsWith("0")
                || normalized.startsWith("62")
                || normalized.startsWith("620")
                || normalized.startsWith("6200")) {
            return false;
        }
        if (!normalized.chars().allMatch(Character::isDigit)) {
            return false;
        }
        if (!normalized.startsWith("8")) {
            return false;
        }
        return normalized.length() >= 9 && normalized.length() <= 32;
    }
}
