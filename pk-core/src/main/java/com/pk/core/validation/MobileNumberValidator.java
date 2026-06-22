package com.pk.core.validation;

public final class MobileNumberValidator {
    private MobileNumberValidator() {
    }

    public static boolean isValid(String mobileNo) {
        if (mobileNo == null || mobileNo.isBlank() || mobileNo.length() > 32) {
            return false;
        }
        if (mobileNo.contains(" ")) {
            return false;
        }
        if (mobileNo.startsWith("+86")
                || mobileNo.startsWith("0")
                || mobileNo.startsWith("62")
                || mobileNo.startsWith("620")
                || mobileNo.startsWith("6200")) {
            return false;
        }
        if (!mobileNo.chars().allMatch(Character::isDigit)) {
            return false;
        }
        return mobileNo.length() >= 9;
    }
}
