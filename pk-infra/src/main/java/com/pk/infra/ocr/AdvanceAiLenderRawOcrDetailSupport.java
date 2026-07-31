package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;

/**
 * Normalizes Advance.ai OCR JSON into the shape expected by lender {@code rawOcrDetail} validation.
 */
public final class AdvanceAiLenderRawOcrDetailSupport {
    private AdvanceAiLenderRawOcrDetailSupport() {
    }

    public static String prepareForLender(ObjectMapper objectMapper, String advanceAiJson) {
        if (advanceAiJson == null || advanceAiJson.isBlank()) {
            return advanceAiJson == null ? "" : advanceAiJson;
        }
        try {
            JsonNode root = objectMapper.readTree(advanceAiJson);
            if (root.has("data") && root.get("data").isObject()) {
                ObjectNode envelope = root.deepCopy();
                envelope.set("data", normalizeDataNode(objectMapper, root.get("data")));
                return objectMapper.writeValueAsString(envelope);
            }
            if (root.isObject()) {
                ObjectNode envelope = objectMapper.createObjectNode();
                envelope.put("code", "SUCCESS");
                envelope.put("message", "OK");
                envelope.set("data", normalizeDataNode(objectMapper, root));
                return objectMapper.writeValueAsString(envelope);
            }
            return advanceAiJson;
        } catch (Exception exception) {
            return advanceAiJson;
        }
    }

    public static JsonNode extractDataNode(ObjectMapper objectMapper, String advanceAiJson) {
        if (advanceAiJson == null || advanceAiJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode root = objectMapper.readTree(advanceAiJson);
            if (root.has("data") && root.get("data").isObject()) {
                return root.get("data");
            }
            return root;
        } catch (Exception exception) {
            return objectMapper.createObjectNode();
        }
    }

    private static ObjectNode normalizeDataNode(ObjectMapper objectMapper, JsonNode data) {
        ObjectNode normalized = data.deepCopy();
        copyIdNumberAlias(normalized);
        removeEmptyTextFields(normalized);
        return normalized;
    }

    private static void copyIdNumberAlias(ObjectNode data) {
        JsonNode idNumber = data.get("idNumber");
        if (idNumber != null && !idNumber.isNull() && !idNumber.asText("").isBlank() && !data.hasNonNull("ktpIdNumber")) {
            data.put("ktpIdNumber", idNumber.asText().trim());
        }
    }

    private static void removeEmptyTextFields(ObjectNode data) {
        Iterator<Map.Entry<String, JsonNode>> fields = data.properties().iterator();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode value = entry.getValue();
            if (value != null && value.isTextual() && value.asText("").isBlank()) {
                fields.remove();
            }
        }
    }
}
