package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.ProfileSyncModule;
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

    static String formatLogBody(String value, int maxBodyBytes) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String sanitized = sanitizeLineBreaks(value);
        if (sanitized.length() <= maxBodyBytes) {
            return sanitized;
        }
        return sanitized.substring(0, maxBodyBytes)
                + "...[truncated "
                + (sanitized.length() - maxBodyBytes)
                + " bytes]";
    }

    static String redactSensitiveJson(String value) {
        if (value == null || value.isBlank()) {
            return value == null ? null : "";
        }
        return value
                .replaceAll("\"clientSecret\"\\s*:\\s*\"[^\"]*\"", "\"clientSecret\":\"***\"")
                .replaceAll("\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"***\"");
    }

    private static String sanitizeLineBreaks(String value) {
        return value.replace('\n', ' ').replace('\r', ' ');
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
        if ("999998".equals(responseCode) || "999999".equals(responseCode)) {
            return new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return PendanaanLenderCodeMapper.toApiException(responseCode, null, ProfileSyncModule.PERSONAL);
    }

    static void ensureSuccess(JsonNode envelope) {
        String responseCode = textOrEmpty(envelope.get("code"));
        if (!ApiCode.SUCCESS.code().equals(responseCode)) {
            throw mapFailureCode(responseCode);
        }
    }
}
