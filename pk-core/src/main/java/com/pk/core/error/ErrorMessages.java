package com.pk.core.error;

import java.util.Map;

public final class ErrorMessages {
    private static final Map<String, String> MESSAGES = Map.ofEntries(
            Map.entry(AppErrorCodes.SUCCESS, "success"),
            Map.entry(AppErrorCodes.INVALID_REQUEST, "Invalid request parameters"),
            Map.entry(AppErrorCodes.UNAUTHORIZED, "Unauthorized request"),
            Map.entry(AppErrorCodes.DEVICE_NO_REQUIRED, "deviceNo is required"),
            Map.entry(AppErrorCodes.INVALID_MOBILE, "Invalid mobile number"),
            Map.entry(AppErrorCodes.TOO_MANY_REQUESTS, "Too many requests"),
            Map.entry(AppErrorCodes.INVALID_OTP, "Invalid or expired verification code"),
            Map.entry(AppErrorCodes.OTP_TOKEN_MISMATCH, "OTP token does not match mobile number"),
            Map.entry(AppErrorCodes.PROFILE_SYNC_FAILED, "Profile sync failed"),
            Map.entry(AppErrorCodes.QUOTE_EXPIRED, "Quote expired"),
            Map.entry(AppErrorCodes.SERVICE_UNAVAILABLE, "Service unavailable"),
            Map.entry(AppErrorCodes.INTERNAL_ERROR, "Internal server error")
    );

    private ErrorMessages() {
    }

    public static String forCode(String code) {
        return MESSAGES.getOrDefault(code, "Internal server error");
    }
}
