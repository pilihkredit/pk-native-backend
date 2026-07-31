package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Local/dev only: direct lender identity upsert. When {@code idCardBase64} is provided and
 * {@code rawOcrDetail} is omitted, the server calls Advance.ai OCR to obtain lender-ready raw JSON.
 */
public record IdentityOcrDevLenderSyncRequest(
        @NotBlank @Size(max = 64) String requestId,
        String faceBase64,
        String idCardBase64,
        String rawOcrDetail,
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
        String district,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
