package com.pk.core.profile.ocr;

/**
 * Request-scoped context for OCR vendor call auditing.
 */
public record OcrCallContext(
        Long profileId,
        String partnerUserId,
        String mobileNo,
        String clientRequestId,
        String traceId
) {
    public static OcrCallContext of(long profileId) {
        return new OcrCallContext(profileId, null, null, null, null);
    }
}
