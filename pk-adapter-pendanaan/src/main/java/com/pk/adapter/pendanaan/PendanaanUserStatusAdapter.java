package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.home.port.LenderUserStatusPort;

public class PendanaanUserStatusAdapter implements LenderUserStatusPort {
    static final String USER_STATUS_PATH = PendanaanOpenApiPaths.USER_STATUS;
    static final String BUSINESS_TYPE = "USER_STATUS";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanUserStatusAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderUserStatusResult queryStatus(LenderUserStatusCommand command) {
        String requestBody = PendanaanUserStatusRequestMapper.buildBody(command);
        JsonNode data = httpClient.post(
                USER_STATUS_PATH,
                requestBody,
                BUSINESS_TYPE,
                command.partnerUserId()
        );
        return new LenderUserStatusResult(
                textOrNull(data.get("partnerUserId")),
                textOrNull(data.get("userId")),
                intOrNull(data.get("userLoanLifeTimeStatus")),
                intOrNull(data.get("userLoanLifeTimeLastAction")),
                longOrNull(data.get("freezeEndTime")),
                booleanOrNull(data.get("firstLoan")),
                booleanOrNull(data.get("firstCreditApply")),
                booleanOrNull(data.get("firstLoanApply")),
                intOrNull(data.get("onLoanCount")),
                longOrNull(data.get("creditContractExpireTime")),
                booleanOrNull(data.get("autoCredit")),
                requestBody,
                serializeResponseData(data)
        );
    }

    private String serializeResponseData(JsonNode data) {
        if (data == null || data.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
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

    private static Boolean booleanOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asBoolean();
    }
}
