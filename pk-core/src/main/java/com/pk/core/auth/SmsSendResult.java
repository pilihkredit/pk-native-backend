package com.pk.core.auth;

public record SmsSendResult(
        boolean success,
        String providerCode,
        String providerMessageId,
        String errorCode,
        String errorMessage
) {
    public static SmsSendResult success(String providerCode, String providerMessageId) {
        return new SmsSendResult(true, providerCode, providerMessageId, null, null);
    }

    public static SmsSendResult failure(String providerCode, String errorCode, String errorMessage) {
        return new SmsSendResult(false, providerCode, null, errorCode, errorMessage);
    }
}
