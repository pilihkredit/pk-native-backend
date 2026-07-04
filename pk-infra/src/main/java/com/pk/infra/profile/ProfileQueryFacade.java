package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.LenderProfileQueryPort;
import java.util.List;

public class ProfileQueryFacade {
    private final LenderProfileQueryPort lenderProfileQueryPort;
    private final ObjectMapper objectMapper;

    public ProfileQueryFacade(
            LenderProfileQueryPort lenderProfileQueryPort,
            ObjectMapper objectMapper
    ) {
        this.lenderProfileQueryPort = lenderProfileQueryPort;
        this.objectMapper = objectMapper;
    }

    public JsonNode query(String partnerUserId, List<String> modules) {
        LenderProfileQueryPort.LenderProfileQueryResult result = lenderProfileQueryPort.query(
                new LenderProfileQueryPort.LenderProfileQueryCommand(partnerUserId, modules)
        );
        return parseResponse(result.rawResponseJson());
    }

    private JsonNode parseResponse(String rawResponseJson) {
        if (rawResponseJson == null || rawResponseJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(rawResponseJson);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
