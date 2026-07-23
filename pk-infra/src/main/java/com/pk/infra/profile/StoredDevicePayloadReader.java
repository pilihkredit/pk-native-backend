package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.DeviceOtherInfoDocumentFields;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class StoredDevicePayloadReader {
    private StoredDevicePayloadReader() {
    }

    public static LenderDeviceContext toLenderDevice(
            ProfileDeviceData stored,
            String lenderAppName,
            ObjectMapper objectMapper
    ) {
        if (lenderAppName == null || lenderAppName.isBlank()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        try {
            JsonNode root = objectMapper.readTree(stored.deviceJson());
            return new LenderDeviceContext(
                    lenderAppName.trim(),
                    textOrDefault(root, "appVersion", stored.appVersion()),
                    textOrDefault(root, "packageName", stored.packageName()),
                    textOrDefault(root, "deviceNo", stored.deviceNo()),
                    textOrDefault(root, "systemPlatform", stored.systemPlatform()),
                    textOrNull(root.get("adId")),
                    readDeviceOtherInfo(root.get("deviceOtherInfo")),
                    textOrDefault(root, "appName", stored.appName()),
                    readExtendedAttributes(root)
            );
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "Stored device payload is invalid");
        }
    }

    private static DeviceExtendedAttributes readExtendedAttributes(JsonNode root) {
        return new DeviceExtendedAttributes(
                textOrNull(root.get("phoneBrand")),
                textOrNull(root.get("phoneBrandModel")),
                textOrNull(root.get("mac")),
                textOrNull(root.get("systemVersion")),
                textOrNull(root.get("deliveryPlatform")),
                intOrNull(root.get("cpuCores")),
                longOrNull(root.get("memoryTotal")),
                longOrNull(root.get("sdCardTotal")),
                textOrNull(root.get("idfv")),
                textOrNull(root.get("idfa")),
                textOrNull(root.get("extParam")),
                textOrNull(root.get("adChannel"))
        );
    }

    private static Map<String, Object> readDeviceOtherInfo(JsonNode node) {
        if (node == null || node.isNull() || !node.isObject()) {
            return null;
        }
        Map<String, Object> values = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            values.put(field.getKey(), objectValue(field.getValue()));
        }
        Map<String, Object> filtered = DeviceOtherInfoDocumentFields.filter(values);
        return filtered.isEmpty() ? null : filtered;
    }

    private static Object objectValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node.asText();
    }

    private static String textOrDefault(JsonNode root, String field, String fallback) {
        String value = textOrNull(root.get(field));
        if (value != null) {
            return value;
        }
        if (fallback == null || fallback.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "device." + field + " is missing");
        }
        return fallback.trim();
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value.trim();
    }

    private static Integer intOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asInt();
    }

    private static Long longOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asLong();
    }
}
