package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.reference.AreaReference;
import com.pk.core.reference.port.LenderAreaPort;
import java.util.ArrayList;
import java.util.List;

public class PendanaanAreaAdapter implements LenderAreaPort {
    static final String AREA_LIST_PATH = PendanaanOpenApiPaths.AREA_LIST;
    static final String BUSINESS_TYPE = "REFERENCE_AREA_LIST";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanAreaAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AreaReference> listAreas(String parentCode) {
        String requestBody = buildRequestBody(parentCode);
        String businessId = parentCode == null || parentCode.isBlank() ? "" : parentCode.trim();
        JsonNode data = httpClient.post(AREA_LIST_PATH, requestBody, BUSINESS_TYPE, businessId);
        if (data == null || !data.isArray()) {
            return List.of();
        }
        List<AreaReference> areas = new ArrayList<>(data.size());
        for (JsonNode item : data) {
            String areaCode = item.path("code").asText("");
            String areaName = item.path("name").asText("");
            String responseParent = item.path("parentCode").asText("");
            if (areaCode.isBlank() || areaName.isBlank()) {
                continue;
            }
            areas.add(new AreaReference(areaCode, areaName, responseParent, 0));
        }
        return List.copyOf(areas);
    }

    private String buildRequestBody(String parentCode) {
        try {
            if (parentCode == null || parentCode.isBlank()) {
                return "{}";
            }
            return objectMapper.writeValueAsString(new AreaListRequest(parentCode.trim()));
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize area list request", exception);
        }
    }

    private record AreaListRequest(String parentCode) {
    }
}
