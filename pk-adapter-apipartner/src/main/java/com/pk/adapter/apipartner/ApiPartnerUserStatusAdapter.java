package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.home.port.LenderUserStatusPort;

public class ApiPartnerUserStatusAdapter implements LenderUserStatusPort {
    static final String USER_STATUS_PATH = ApiPartnerOpenApiPaths.USER_STATUS;
    static final String BUSINESS_TYPE = "USER_STATUS";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerUserStatusAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderUserStatusResult queryStatus(LenderUserStatusCommand command) {
        String requestBody = ApiPartnerUserStatusRequestMapper.buildBody(command);
        ApiPartnerHttpClient.ExchangeResult exchange = httpClient.postWithInteraction(
                USER_STATUS_PATH,
                requestBody,
                BUSINESS_TYPE,
                command.partnerUserId()
        );
        JsonNode data = exchange.data();
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
                exchange.interactionId()
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
