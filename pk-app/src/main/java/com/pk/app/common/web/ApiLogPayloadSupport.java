package com.pk.app.common.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.regex.Pattern;

/**
 * Redacts lender-side text from API access logs while keeping PK envelope messages.
 */
final class ApiLogPayloadSupport {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern LENDER_DETAIL_PREFIX = Pattern.compile("^A\\d{6}:.*");
    private static final Pattern CJK = Pattern.compile(".*\\p{Script=Han}.*");

    private ApiLogPayloadSupport() {
    }

    static String redactForLog(String payload) {
        if (payload == null || payload.isBlank()) {
            return payload == null ? "" : payload;
        }
        try {
            JsonNode root = MAPPER.readTree(payload);
            redactNode(root);
            return MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            return payload;
        }
    }

    private static void redactNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            boolean pkEnvelope = isPkResponseEnvelope(objectNode);
            objectNode.fieldNames().forEachRemaining(field -> {
                JsonNode child = objectNode.get(field);
                if ("msg".equals(field) && child != null && child.isTextual()) {
                    String message = child.asText();
                    if (!pkEnvelope || shouldRedactLenderMessage(message)) {
                        objectNode.put(field, "[redacted]");
                    }
                    return;
                }
                redactNode(child);
            });
            return;
        }
        if (node.isArray()) {
            node.forEach(ApiLogPayloadSupport::redactNode);
        }
    }

    private static boolean isPkResponseEnvelope(ObjectNode node) {
        return node.hasNonNull("code") && node.has("msg") && (node.has("traceId") || node.has("data"));
    }

    private static boolean shouldRedactLenderMessage(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String trimmed = message.trim();
        return LENDER_DETAIL_PREFIX.matcher(trimmed).matches() || CJK.matcher(trimmed).matches();
    }
}
