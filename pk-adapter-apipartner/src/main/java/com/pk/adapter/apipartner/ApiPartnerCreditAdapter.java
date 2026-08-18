package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.port.LenderCreditPort;
import java.math.BigDecimal;

public class ApiPartnerCreditAdapter implements LenderCreditPort {
    static final String APPLY_PATH = ApiPartnerOpenApiPaths.CREDIT_APPLY;
    static final String APPLY_STATUS_PATH = ApiPartnerOpenApiPaths.CREDIT_APPLY_STATUS;
    static final String BUSINESS_TYPE_APPLY = "CREDIT_APPLY";
    static final String BUSINESS_TYPE_STATUS = "CREDIT_APPLY_STATUS";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerCreditAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderCreditApplyResult apply(LenderCreditApplyCommand command) {
        String requestBody = ApiPartnerCreditRequestMapper.buildApplyBody(command);
        ApiPartnerHttpClient.EnvelopeResult exchange = httpClient.postEnvelopeWithInteraction(
                APPLY_PATH,
                requestBody,
                BUSINESS_TYPE_APPLY,
                command.applyId()
        );
        JsonNode envelope = exchange.envelope();
        String responseCode = ApiPartnerHttpSupport.textOrEmpty(envelope.get("code"));
        if (!ApiCode.SUCCESS.code().equals(responseCode)) {
            throw ApiPartnerHttpSupport.mapFailureCode(
                    responseCode,
                    ApiPartnerHttpSupport.textOrEmpty(envelope.get("msg"))
            );
        }
        JsonNode data = envelope.get("data");
        if (data == null || data.isNull()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return new LenderCreditApplyResult(
                ApiPartnerHttpSupport.textOrEmpty(data.get("creditApplyNo")),
                textOrNull(data.get("userId")),
                exchange.interactionId()
        );
    }

    @Override
    public LenderCreditStatusResult queryStatus(String applyId) {
        String requestBody = ApiPartnerCreditRequestMapper.buildStatusBody(applyId);
        ApiPartnerHttpClient.ExchangeResult exchange = httpClient.postWithInteraction(
                APPLY_STATUS_PATH,
                requestBody,
                BUSINESS_TYPE_STATUS,
                applyId
        );
        JsonNode data = exchange.data();
        return new LenderCreditStatusResult(
                ApiPartnerHttpSupport.textOrEmpty(data.get("status")),
                textOrNull(data.get("userId")),
                textOrNull(data.get("creditApplyNo")),
                longOrNull(data.get("creditContractExpireTime")),
                longOrNull(data.get("freezeEndTime")),
                decimalOrNull(data.get("riskMinLimit")),
                decimalOrNull(data.get("riskMaxLimit")),
                decimalOrNull(data.get("psychologicalCreditLimit")),
                decimalOrNull(data.get("fakeCreditLimit")),
                decimalOrNull(data.get("borrowAmtStepSize")),
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

    private static Long longOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asLong();
    }

    private static BigDecimal decimalOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }
}
