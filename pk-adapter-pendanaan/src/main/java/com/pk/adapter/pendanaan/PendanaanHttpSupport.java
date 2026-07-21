package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.ProfileSyncModule;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

final class PendanaanHttpSupport {
    static final String ACCEPT_LANGUAGE = "in-ID";
    static final int STORAGE_REF_MAX_CHARS = 256 * 1024;
    private static final int SENSITIVE_PREVIEW_LENGTH = 100;
    private static final ObjectMapper LOG_MAPPER = new ObjectMapper();
    private static final Set<String> SENSITIVE_LOG_FIELDS = Set.of(
            "clientSecret",
            "accessToken",
            "token",
            "refreshToken",
            "faceBase64",
            "idCardBase64",
            "imageBase64",
            "faceImageBase64",
            "idCardImageBase64"
    );

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
        return value.length() <= STORAGE_REF_MAX_CHARS ? value : value.substring(0, STORAGE_REF_MAX_CHARS);
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
        try {
            JsonNode root = LOG_MAPPER.readTree(value);
            redactSensitiveNode(root);
            return LOG_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            return previewSensitiveValue(value);
        }
    }

    private static void redactSensitiveNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(field -> {
                JsonNode child = objectNode.get(field);
                if (child != null && child.isTextual() && SENSITIVE_LOG_FIELDS.contains(field)) {
                    objectNode.put(field, previewSensitiveValue(child.asText()));
                } else {
                    redactSensitiveNode(child);
                }
            });
            return;
        }
        if (node.isArray()) {
            node.forEach(PendanaanHttpSupport::redactSensitiveNode);
        }
    }

    private static String previewSensitiveValue(String value) {
        if (value == null) {
            return "";
        }
        String normalized = sanitizeLineBreaks(value);
        if (normalized.length() <= SENSITIVE_PREVIEW_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, SENSITIVE_PREVIEW_LENGTH) + "...[truncated]";
    }

    private static String sanitizeLineBreaks(String value) {
        return value.replace('\n', ' ').replace('\r', ' ');
    }

    static String extractMobileNo(String requestBody) {
        if (requestBody == null || requestBody.isBlank()) {
            return null;
        }
        try {
            JsonNode root = LOG_MAPPER.readTree(requestBody);
            return findMobileNoNode(root);
        } catch (Exception exception) {
            return null;
        }
    }

    private static String findMobileNoNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            JsonNode direct = node.get("mobileNo");
            if (direct != null && direct.isTextual()) {
                String value = direct.asText("").trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                String nested = findMobileNoNode(entry.getValue());
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                String nested = findMobileNoNode(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    static String formatTransportFailure(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String className = root.getClass().getSimpleName();
        String message = root.getMessage();
        if (message == null || message.isBlank()) {
            return className;
        }
        return className + ": " + sanitizeLineBreaks(message);
    }

    static int requestBodyBytes(String requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            return 0;
        }
        return requestBody.getBytes(StandardCharsets.UTF_8).length;
    }

    static void applyTransportFailureForLog(InteractionOutcome outcome) {
        if (outcome.success || !outcome.responseText.isEmpty()) {
            return;
        }
        if (outcome.transportFailure != null && !outcome.transportFailure.isBlank()) {
            outcome.responseCode = "TRANSPORT_ERROR";
            outcome.responseMsg = outcome.transportFailure;
            return;
        }
        if (outcome.httpStatus != null) {
            outcome.responseCode = "HTTP_" + outcome.httpStatus;
            outcome.responseMsg = "HTTP status " + outcome.httpStatus + " with empty body";
        }
    }

    static final class InteractionOutcome {
        String responseText = "";
        String responseCode = "";
        String responseMsg = "";
        boolean success = false;
        String transportFailure = "";
        Integer httpStatus;
        Long interactionId;
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
        return mapFailureCode(responseCode, null);
    }

    static ApiException mapFailureCode(String responseCode, String responseMsg) {
        if ("999998".equals(responseCode) || "999999".equals(responseCode)) {
            return new ApiException(ApiCode.SERVICE_UNAVAILABLE, normalizeLenderMessage(responseMsg));
        }
        return PendanaanLenderCodeMapper.toApiException(
                responseCode,
                normalizeLenderMessage(responseMsg),
                ProfileSyncModule.PERSONAL
        );
    }

    static void ensureSuccess(JsonNode envelope) {
        String responseCode = textOrEmpty(envelope.get("code"));
        if (!ApiCode.SUCCESS.code().equals(responseCode)) {
            throw mapFailureCode(responseCode, textOrEmpty(envelope.get("msg")));
        }
    }

    private static String normalizeLenderMessage(String responseMsg) {
        if (responseMsg == null || responseMsg.isBlank()) {
            return null;
        }
        return responseMsg.trim();
    }
}
