package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.sync.ProfileSyncModule;

public class PendanaanProfileSyncAdapter implements LenderProfileSyncPort {
    static final String UPSERT_PATH = "/api/open/v1/user/info/upsert";
    static final String BUSINESS_TYPE = "PROFILE_SYNC";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanProfileSyncAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderProfileSyncResult syncModule(LenderProfileSyncCommand command) {
        String requestBody = buildRequestBody(command);
        JsonNode envelope = httpClient.postEnvelope(
                UPSERT_PATH,
                requestBody,
                BUSINESS_TYPE,
                command.partnerUserId()
        );
        String responseCode = PendanaanHttpSupport.textOrEmpty(envelope.get("code"));
        if (ApiCode.SUCCESS.code().equals(responseCode)) {
            return new LenderProfileSyncResult(true);
        }
        throw PendanaanProfileCodeMapper.toApiException(
                responseCode,
                command.module() == ProfileSyncModule.BANK_CARD
        );
    }

    private String buildRequestBody(LenderProfileSyncCommand command) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("requestId", command.requestId());
            root.put("partnerUserId", command.partnerUserId());
            ObjectNode userInfo = objectMapper.createObjectNode();
            PendanaanProfileUpsertMapper.applyModule(userInfo, command.module(), command.payload());
            PendanaanProfileUpsertMapper.applyDevice(userInfo, command.device());
            root.set("userInfo", userInfo);
            return objectMapper.writeValueAsString(root);
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
