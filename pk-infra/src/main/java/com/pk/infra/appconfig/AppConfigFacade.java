package com.pk.infra.appconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;

public class AppConfigFacade {
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
        AppConfigRepository.AppConfigRecord record = appConfigRepository.findByKey(key.trim())
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
        try {
            return objectMapper.readTree(record.valueJson());
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }
}
