package com.pk.infra.appconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.util.Set;

public class AppConfigFacade {
    private static final Set<String> PRIVATE_KEYS = Set.of(
            "advanceAiConf",
            "trustDecisionConf",
            "reviewSandboxConf"
    );
    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public AppConfigFacade(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public JsonNode getValueByKey(String key) {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        String normalizedKey = key.trim();
        if (PRIVATE_KEYS.contains(normalizedKey)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        AppConfigRepository.AppConfigRecord record = appConfigRepository.findByKey(normalizedKey)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
        try {
            return objectMapper.readTree(record.valueJson());
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }
}
