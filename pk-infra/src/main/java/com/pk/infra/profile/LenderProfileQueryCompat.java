package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Maps lender user/info/query bank card payload to the legacy {@code bankCard} object
 * so existing app clients keep working after lender v1.1.12 ({@code bankCardList}).
 */
final class LenderProfileQueryCompat {
    private LenderProfileQueryCompat() {
    }

    static JsonNode applyBankCardCompat(JsonNode root, ObjectMapper objectMapper) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return objectMapper.createObjectNode();
        }
        if (!(root instanceof ObjectNode object)) {
            return root;
        }
        JsonNode list = object.get("bankCardList");
        if (list == null || list.isNull() || list.isMissingNode() || !list.isArray() || list.isEmpty()) {
            return object;
        }
        JsonNode selected = selectDefaultOrFirst(list);
        if (selected == null || !selected.isObject()) {
            return object;
        }
        ObjectNode bankCard = object.putObject("bankCard");
        copyText(selected, bankCard, "bankCode");
        copyText(selected, bankCard, "bankName");
        copyText(selected, bankCard, "cardNumber");
        // Legacy clients may still read cardName; lender removed it — mirror bankName when present.
        if (selected.hasNonNull("bankName") && !bankCard.hasNonNull("cardName")) {
            bankCard.put("cardName", selected.get("bankName").asText());
        }
        return object;
    }

    private static JsonNode selectDefaultOrFirst(JsonNode list) {
        JsonNode fallback = null;
        for (JsonNode item : list) {
            if (item == null || !item.isObject()) {
                continue;
            }
            if (fallback == null) {
                fallback = item;
            }
            JsonNode isDefault = item.get("isDefault");
            if (isDefault != null && isDefault.isBoolean() && isDefault.booleanValue()) {
                return item;
            }
        }
        return fallback;
    }

    private static void copyText(JsonNode source, ObjectNode target, String field) {
        JsonNode value = source.get(field);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return;
        }
        if (value.isTextual()) {
            target.put(field, value.asText());
        } else {
            target.set(field, value);
        }
    }
}
