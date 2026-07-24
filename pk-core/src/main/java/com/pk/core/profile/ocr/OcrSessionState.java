package com.pk.core.profile.ocr;

import java.time.Instant;

public record OcrSessionState(
        boolean licenseObtained,
        boolean ocrCheckCompleted,
        boolean livenessPassed,
        Integer livenessScore,
        String ocrRawJson,
        OcrParsedFields parsed,
        String idCardImageEncryptedRef,
        Long ocrCheckVendorCallLogId,
        Instant updatedAt
) {
    public record OcrParsedFields(
            String ocrName,
            String ocrIdNo,
            String gender,
            String religion,
            String maritalStatus,
            String birthday,
            String birthPlace,
            String address,
            String occupation,
            String nationality,
            String bloodType,
            String expiryDate,
            String province,
            String city,
            String district
    ) {
    }
}
