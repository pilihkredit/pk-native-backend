package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.sync.ProfileSyncModule;

public class PendanaanProfileSyncAdapter implements LenderProfileSyncPort {
    static final String UPSERT_PATH = PendanaanOpenApiPaths.USER_INFO_UPSERT;
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
        PendanaanHttpClient.EnvelopeResult exchange = httpClient.postEnvelopeWithInteraction(
                UPSERT_PATH,
                requestBody,
                BUSINESS_TYPE,
                command.partnerUserId()
        );
        JsonNode envelope = exchange.envelope();
        String responseCode = PendanaanHttpSupport.textOrEmpty(envelope.get("code"));
        if (ApiCode.SUCCESS.code().equals(responseCode)) {
            JsonNode data = envelope.get("data");
            String externalUserId = data == null || data.isNull()
                    ? null
                    : PendanaanJsonSupport.textOrNull(data.get("userId"));
            return new LenderProfileSyncResult(
                    externalUserId,
                    serializeResponseData(data),
                    exchange.interactionId()
            );
        }
        throw PendanaanProfileCodeMapper.toApiException(
                responseCode,
                PendanaanHttpSupport.textOrEmpty(envelope.get("msg")),
                command.module()
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

    private String buildRequestBody(LenderProfileSyncCommand command) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("requestId", command.requestId());
            root.put("partnerUserId", command.partnerUserId());
            ObjectNode userInfo = objectMapper.createObjectNode();
            PendanaanProfileUpsertMapper.applyMobileNo(userInfo, command.mobileNo());
            PendanaanProfileUpsertMapper.applyModule(
                    userInfo, command.module(), command.payload(), command.device()
            );
            for (LenderProfileSyncPort.SyncCompanion companion : command.companions()) {
                PendanaanProfileUpsertMapper.applyModule(
                        userInfo, companion.module(), companion.payload(), command.device()
                );
            }
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
