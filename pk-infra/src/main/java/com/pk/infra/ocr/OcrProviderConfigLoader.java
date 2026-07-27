package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.time.Duration;

public class OcrProviderConfigLoader {
    public static final String ADVANCE_AI_KEY = "advanceAiConf";
    public static final String TRUST_DECISION_KEY = "trustDecisionConf";

    private final AppConfigRepository repository;
    private final ObjectMapper objectMapper;

    public OcrProviderConfigLoader(AppConfigRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public OcrProperties loadAdvanceAi() {
        JsonNode root = loadEnabledObject(ADVANCE_AI_KEY);
        OcrProperties properties = new OcrProperties();
        properties.setEnabled(true);
        properties.setAccessKey(requiredText(root, "accessKey", ADVANCE_AI_KEY));
        properties.setSecretKey(requiredText(root, "secretKey", ADVANCE_AI_KEY));
        properties.setAccessTokenUrl(text(root, "accessTokenUrl", properties.accessTokenUrl()));
        properties.setDatabaseIdValidationUrl(text(
                root, "databaseIdValidationUrl", properties.databaseIdValidationUrl()));
        properties.setBaseUrl(text(root, "baseUrl", properties.baseUrl()));
        properties.setLicenseUrl(text(root, "licenseUrl", properties.licenseUrl()));
        properties.setOcrCheckUrl(text(root, "ocrCheckUrl", properties.ocrCheckUrl()));
        properties.setLivenessDetectionUrl(text(
                root, "livenessDetectionUrl", properties.livenessDetectionUrl()));
        properties.setFaceRecognitionUrl(text(
                root, "faceRecognitionUrl", properties.faceRecognitionUrl()));
        properties.setTokenKeyPrefix(text(root, "tokenKeyPrefix", properties.tokenKeyPrefix()));
        properties.setTokenCacheSeconds(longValue(root, "tokenCacheSeconds", properties.tokenCacheSeconds()));
        properties.setLicenseEffectiveSeconds(longValue(
                root, "licenseEffectiveSeconds", properties.licenseEffectiveSeconds()));
        properties.setLivenessThreshold(intValue(root, "livenessThreshold", properties.livenessThreshold()));
        properties.setFaceThreshold(intValue(root, "faceThreshold", properties.faceThreshold()));
        properties.setMaxImageBytes(intValue(root, "maxImageBytes", properties.maxImageBytes()));
        properties.setConnectTimeoutMs(intValue(root, "connectTimeoutMs", properties.connectTimeoutMs()));
        properties.setReadTimeoutMs(intValue(root, "readTimeoutMs", properties.readTimeoutMs()));
        properties.setSessionTtl(Duration.ofSeconds(longValue(
                root, "sessionTtlSeconds", properties.sessionTtl().toSeconds())));
        properties.setDevLenderSyncEnabled(booleanValue(root, "devLenderSyncEnabled", false));
        return properties;
    }

    public TrustDecisionProperties loadTrustDecision() {
        JsonNode root = loadEnabledObject(TRUST_DECISION_KEY);
        TrustDecisionProperties properties = new TrustDecisionProperties();
        properties.setEnabled(true);
        properties.setPartnerCode(requiredText(root, "partnerCode", TRUST_DECISION_KEY));
        properties.setPartnerKey(requiredText(root, "partnerKey", TRUST_DECISION_KEY));
        properties.setOcrUrl(text(root, "ocrUrl", properties.ocrUrl()));
        properties.setLivenessLicenseUrl(text(
                root, "livenessLicenseUrl", properties.livenessLicenseUrl()));
        properties.setLivenessResultUrl(text(
                root, "livenessResultUrl", properties.livenessResultUrl()));
        properties.setConnectTimeoutMs(intValue(root, "connectTimeoutMs", properties.connectTimeoutMs()));
        properties.setReadTimeoutMs(intValue(root, "readTimeoutMs", properties.readTimeoutMs()));
        properties.setMaxImageBytes(intValue(root, "maxImageBytes", properties.maxImageBytes()));
        properties.setSessionTtl(Duration.ofSeconds(longValue(
                root, "sessionTtlSeconds", properties.sessionTtl().toSeconds())));
        return properties;
    }

    private JsonNode loadEnabledObject(String key) {
        var record = repository.findByKey(key)
                .orElseThrow(() -> unavailable(key + " config missing"));
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            if (root == null || !root.isObject()) {
                throw unavailable(key + " config invalid");
            }
            if (!booleanValue(root, "enabled", false)) {
                throw unavailable(key + " is disabled");
            }
            return root;
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw unavailable(key + " config invalid");
        }
    }

    private static String requiredText(JsonNode root, String field, String key) {
        String value = text(root, field, "");
        if (value.isBlank()) {
            throw unavailable(key + " credential missing");
        }
        return value;
    }

    private static String text(JsonNode root, String field, String defaultValue) {
        JsonNode node = root.get(field);
        return node == null || node.isNull() || node.asText().isBlank()
                ? defaultValue
                : node.asText().trim();
    }

    private static int intValue(JsonNode root, String field, int defaultValue) {
        JsonNode node = root.get(field);
        return node != null && node.canConvertToInt() && node.asInt() > 0 ? node.asInt() : defaultValue;
    }

    private static long longValue(JsonNode root, String field, long defaultValue) {
        JsonNode node = root.get(field);
        return node != null && node.canConvertToLong() && node.asLong() > 0 ? node.asLong() : defaultValue;
    }

    private static boolean booleanValue(JsonNode root, String field, boolean defaultValue) {
        JsonNode node = root.get(field);
        return node == null || node.isNull() ? defaultValue : node.asBoolean(defaultValue);
    }

    private static ApiException unavailable(String message) {
        return new ApiException(ApiCode.SERVICE_UNAVAILABLE, message);
    }
}
