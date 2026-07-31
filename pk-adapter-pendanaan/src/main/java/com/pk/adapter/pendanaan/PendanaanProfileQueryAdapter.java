package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.LenderProfileQueryPort;

public class PendanaanProfileQueryAdapter implements LenderProfileQueryPort {
    static final String USER_INFO_QUERY_PATH = PendanaanOpenApiPaths.USER_INFO_QUERY;
    static final String BUSINESS_TYPE = "PROFILE_INFO_QUERY";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanProfileQueryAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderProfileQueryResult query(LenderProfileQueryCommand command) {
        String requestBody = buildRequestBody(command);
        JsonNode data = httpClient.post(
                USER_INFO_QUERY_PATH,
                requestBody,
                BUSINESS_TYPE,
                command.partnerUserId()
        );
        return new LenderProfileQueryResult(serializeResponseData(data));
    }

    private String buildRequestBody(LenderProfileQueryCommand command) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("partnerUserId", command.partnerUserId());
            if (command.modules() != null && !command.modules().isEmpty()) {
                ArrayNode modules = root.putArray("modules");
                for (String module : command.modules()) {
                    modules.add(module);
                }
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    private String serializeResponseData(JsonNode data) {
        if (data == null || data.isNull()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
