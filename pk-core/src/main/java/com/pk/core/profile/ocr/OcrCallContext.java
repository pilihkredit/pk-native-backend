package com.pk.core.profile.ocr;

/**
 * Request-scoped context for OCR vendor call auditing.
 */
public record OcrCallContext(
        Long userId,
        String partnerUserId,
        String mobileNo,
        String clientRequestId,
        String traceId
) {
    public static OcrCallContext of(long userId) {
        return new OcrCallContext(userId, null, null, null, null);
    }

    public static OcrCallContext of(long userId, String mobileNo) {
        return new OcrCallContext(userId, null, mobileNo, null, null);
    }

    public static OcrCallContext of(long userId, String partnerUserId, String mobileNo) {
        return new OcrCallContext(userId, partnerUserId, mobileNo, null, null);
    }

    public static OcrCallContext of(
            long userId,
            String partnerUserId,
            String mobileNo,
            String clientRequestId,
            String traceId
    ) {
        return new OcrCallContext(userId, partnerUserId, mobileNo, clientRequestId, traceId);
    }
}
