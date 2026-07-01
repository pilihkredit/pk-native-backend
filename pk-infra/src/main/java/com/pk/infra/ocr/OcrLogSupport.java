package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class OcrLogSupport {
    static final int PREVIEW_LENGTH = 100;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "imageBase64",
            "faceImageBase64",
            "idCardImageBase64",
            "faceBase64",
            "idCardBase64",
            "license",
            "licenseToken",
            "token",
            "rawOcrDetail",
            "signature"
    );

    private OcrLogSupport() {
    }

    static String preview(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\n', ' ').replace('\r', ' ');
        if (normalized.length() <= PREVIEW_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, PREVIEW_LENGTH) + "...[truncated]";
    }

    public static String redactPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return "";
        }
        try {
            JsonNode root = MAPPER.readTree(payload);
            redactNode(root);
            return MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            return preview(payload);
        }
    }

    static String describeMultipart(Map<String, byte[]> form) {
        if (form == null || form.isEmpty()) {
            return "{}";
        }
        return form.entrySet().stream()
                .map(entry -> entry.getKey() + "=byte[" + entry.getValue().length + "]")
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static void redactNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(field -> {
                JsonNode child = objectNode.get(field);
                if (child != null && child.isTextual() && SENSITIVE_FIELDS.contains(field)) {
                    objectNode.put(field, preview(child.asText()));
                } else {
                    redactNode(child);
                }
            });
            return;
        }
        if (node.isArray()) {
            node.forEach(OcrLogSupport::redactNode);
        }
    }
}
