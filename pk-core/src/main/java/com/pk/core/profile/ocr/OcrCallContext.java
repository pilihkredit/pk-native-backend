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

    public static OcrCallContext of(long profileId, String mobileNo) {
        return new OcrCallContext(profileId, null, mobileNo, null, null);
    }

    public static OcrCallContext of(long profileId, String partnerUserId, String mobileNo) {
        return new OcrCallContext(profileId, partnerUserId, mobileNo, null, null);
    }

    public static OcrCallContext of(
            long profileId,
            String partnerUserId,
            String mobileNo,
            String clientRequestId,
            String traceId
    ) {
        return new OcrCallContext(profileId, partnerUserId, mobileNo, clientRequestId, traceId);
    }
}
