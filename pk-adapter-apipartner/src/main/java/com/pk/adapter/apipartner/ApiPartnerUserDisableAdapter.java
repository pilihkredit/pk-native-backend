package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.port.LenderUserDisablePort;

public class ApiPartnerUserDisableAdapter implements LenderUserDisablePort {
    static final String DISABLE_PATH = ApiPartnerOpenApiPaths.USER_DISABLE;
    static final String DISABLE_BUSINESS_TYPE = "USER_DISABLE";
    private static final String LENDER_ALREADY_DISABLED = "A000036";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerUserDisableAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void disableUser(String partnerUserId) {
        if (partnerUserId == null || partnerUserId.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        try {
            var root = objectMapper.createObjectNode();
            root.put("partnerUserId", partnerUserId.trim());
            String body = objectMapper.writeValueAsString(root);
            ApiPartnerHttpClient.EnvelopeResult envelopeResult = httpClient.postEnvelopeWithInteraction(
                    DISABLE_PATH,
                    body,
                    DISABLE_BUSINESS_TYPE,
                    partnerUserId.trim()
            );
            JsonNode envelope = envelopeResult.envelope();
            String responseCode = ApiPartnerHttpSupport.textOrEmpty(envelope.get("code"));
            if (ApiCode.SUCCESS.code().equals(responseCode) || LENDER_ALREADY_DISABLED.equals(responseCode)) {
                return;
            }
            throw ApiPartnerHttpSupport.mapFailureCode(
                    responseCode,
                    ApiPartnerHttpSupport.textOrEmpty(envelope.get("msg"))
            );
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }
}
