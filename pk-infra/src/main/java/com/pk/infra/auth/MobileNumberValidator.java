package com.pk.infra.auth;

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
        return normalized.length() >= 9;
    }
}
