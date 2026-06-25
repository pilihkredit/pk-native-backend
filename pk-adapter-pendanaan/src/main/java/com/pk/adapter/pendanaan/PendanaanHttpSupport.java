package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

final class PendanaanHttpSupport {
    private PendanaanHttpSupport() {
    }

    static String normalizeBaseUrl(String baseUrl) {
        String trimmed = baseUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    static String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    static String textOrEmpty(JsonNode node) {
        return node == null || node.isNull() ? "" : node.asText("");
    }

    static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 512 ? value : value.substring(0, 512);
    }

    static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    static ApiException mapFailureCode(String responseCode) {
        if ("A000145".equals(responseCode)) {
            return new ApiException(ApiCode.LENDER_LOAN_AMOUNT_REJECTED);
        }
        if ("999998".equals(responseCode) || "999999".equals(responseCode)) {
            return new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    static void ensureSuccess(JsonNode envelope) {
        String responseCode = textOrEmpty(envelope.get("code"));
        if (!ApiCode.SUCCESS.code().equals(responseCode)) {
            throw mapFailureCode(responseCode);
        }
    }
}
