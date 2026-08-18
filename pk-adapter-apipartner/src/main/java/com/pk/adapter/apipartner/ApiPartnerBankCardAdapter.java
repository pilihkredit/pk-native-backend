package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.LenderBankCardPort;

public class ApiPartnerBankCardAdapter implements LenderBankCardPort {
    static final String DELETE_PATH = ApiPartnerOpenApiPaths.USER_BANK_CARD_DELETE;
    static final String DEFAULT_PATH = ApiPartnerOpenApiPaths.USER_BANK_CARD_DEFAULT;
    static final String DELETE_BUSINESS_TYPE = "USER_BANK_CARD_DELETE";
    static final String DEFAULT_BUSINESS_TYPE = "USER_BANK_CARD_DEFAULT";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerBankCardAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void deleteBankCard(DeleteBankCardCommand command) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("partnerUserId", command.partnerUserId());
            root.put("bankCardId", command.bankCardId());
            httpClient.post(
                    DELETE_PATH,
                    objectMapper.writeValueAsString(root),
                    DELETE_BUSINESS_TYPE,
                    command.partnerUserId()
            );
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    @Override
    public void setDefaultBankCard(SetDefaultBankCardCommand command) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("partnerUserId", command.partnerUserId());
            root.put("bankCardId", command.bankCardId());
            httpClient.post(
                    DEFAULT_PATH,
                    objectMapper.writeValueAsString(root),
                    DEFAULT_BUSINESS_TYPE,
                    command.partnerUserId()
            );
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }
}
