package com.pk.infra.ocr;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.util.Base64;

public final class OcrImageSupport {
    private OcrImageSupport() {
    }

    public static byte[] decodeBase64Image(String imageBase64, int maxBytes) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "imageBase64 is required");
        }
        String normalized = imageBase64.trim();
        if (normalized.contains(",")) {
            normalized = normalized.substring(normalized.indexOf(',') + 1);
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(normalized);
            if (bytes.length == 0) {
                throw new ApiException(ApiCode.OCR_IMAGE_INVALID);
            }
            if (bytes.length > maxBytes) {
                throw new ApiException(ApiCode.OCR_IMAGE_INVALID);
            }
            return bytes;
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiCode.OCR_IMAGE_INVALID);
        }
    }

    public static String encodeBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public static String stripDataUriPrefix(String imageBase64) {
        if (imageBase64 == null) {
            return null;
        }
        String normalized = imageBase64.trim();
        if (normalized.contains(",")) {
            return normalized.substring(normalized.indexOf(',') + 1);
        }
        return normalized;
    }
}
