package com.pk.app.profile.dto.response;

public record TrustDecisionOcrCheckResponse(
        String result,
        String sequenceId,
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
