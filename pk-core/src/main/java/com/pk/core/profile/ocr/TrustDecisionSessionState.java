package com.pk.core.profile.ocr;

import java.time.Instant;

public record TrustDecisionSessionState(
        boolean ocrCompleted,
        boolean livenessCompleted,
        String livenessResult,
        Double livenessScore,
        String livenessSequenceId,
        String ocrRawJson,
        OcrSessionState.OcrParsedFields parsed,
        String idCardImageEncryptedRef,
        Long ocrVendorCallLogId,
        Instant updatedAt
) {
}
