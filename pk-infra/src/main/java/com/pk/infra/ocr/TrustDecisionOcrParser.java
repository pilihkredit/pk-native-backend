package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.profile.ocr.OcrSessionState;

public final class TrustDecisionOcrParser {
    private TrustDecisionOcrParser() {
    }

    public static OcrSessionState.OcrParsedFields parse(JsonNode root) {
        if (root == null
                || root.path("code").asInt() != 200
                || !"success".equalsIgnoreCase(text(root, "result"))) {
            return null;
        }
        JsonNode card = root.path("card_info");
        if (!card.isObject()) {
            return null;
        }
        return new OcrSessionState.OcrParsedFields(
                text(card, "name"),
                text(card, "nik"),
                text(card, "gender"),
                text(card, "religion"),
                text(card, "marital_status"),
                text(card, "birthday"),
                text(card, "birthplace"),
                text(card, "address"),
                text(card, "occupation"),
                text(card, "nationality"),
                text(card, "blood_type"),
                text(card, "expiry_date"),
                text(card, "province"),
                text(card, "city"),
                text(card, "street")
        );
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
