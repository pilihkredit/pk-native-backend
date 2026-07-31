package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.LenderBankCardPort;

public class PendanaanBankCardAdapter implements LenderBankCardPort {
    static final String DELETE_PATH = PendanaanOpenApiPaths.USER_BANK_CARD_DELETE;
    static final String BUSINESS_TYPE = "USER_BANK_CARD_DELETE";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanBankCardAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
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
                    BUSINESS_TYPE,
                    command.partnerUserId()
            );
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }
}
