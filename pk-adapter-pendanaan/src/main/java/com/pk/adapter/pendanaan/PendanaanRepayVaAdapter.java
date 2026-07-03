package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.LenderRepayVa;
import com.pk.core.repay.port.LenderRepayVaPort;
import java.util.ArrayList;
import java.util.List;

public class PendanaanRepayVaAdapter implements LenderRepayVaPort {
    static final String VA_LIST_PATH = PendanaanOpenApiPaths.REPAY_VA_LIST;
    static final String VA_DEFAULT_PATH = PendanaanOpenApiPaths.REPAY_VA_DEFAULT;
    static final String BUSINESS_TYPE_LIST = "REPAY_VA_LIST";
    static final String BUSINESS_TYPE_DEFAULT = "REPAY_VA_DEFAULT";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanRepayVaAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderRepayVaListResult listVas(String partnerUserId) {
        String requestBody = "{\"partnerUserId\":\"" + partnerUserId + "\"}";
        JsonNode data = httpClient.post(VA_LIST_PATH, requestBody, BUSINESS_TYPE_LIST, partnerUserId);
        String rawResponseJson = data == null ? "{}" : data.toString();
        JsonNode defaultVaNode = data.get("defaultVa");
        return new LenderRepayVaListResult(
                PendanaanJsonSupport.requireText(data.get("partnerUserId"), "partnerUserId"),
                PendanaanJsonSupport.requireText(data.get("userId"), "userId"),
                defaultVaNode == null || defaultVaNode.isNull() ? null : mapVa(defaultVaNode),
                mapVaList(data.get("vas")),
                requestBody,
                rawResponseJson
        );
    }

    @Override
    public void setDefaultVa(LenderRepayVaDefaultCommand command) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("partnerUserId", command.partnerUserId());
            root.put("vaNo", command.vaNo());
            if (command.bankChannel() != null && !command.bankChannel().isBlank()) {
                root.put("bankChannel", command.bankChannel());
            }
            httpClient.post(
                    VA_DEFAULT_PATH,
                    objectMapper.writeValueAsString(root),
                    BUSINESS_TYPE_DEFAULT,
                    command.partnerUserId()
            );
        } catch (com.pk.core.api.ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private List<LenderRepayVa> mapVaList(JsonNode vasNode) {
        if (vasNode == null || !vasNode.isArray()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderRepayVa> vas = new ArrayList<>();
        for (JsonNode vaNode : vasNode) {
            vas.add(mapVa(vaNode));
        }
        return List.copyOf(vas);
    }

    static LenderRepayVa mapVa(JsonNode vaNode) {
        return new LenderRepayVa(
                PendanaanJsonSupport.requireText(vaNode.get("vaNo"), "vaNo"),
                PendanaanJsonSupport.requireText(vaNode.get("bankCode"), "bankCode"),
                PendanaanJsonSupport.requireText(vaNode.get("bankName"), "bankName"),
                PendanaanJsonSupport.requireInt(vaNode.get("bankType"), "bankType"),
                PendanaanJsonSupport.textOrNull(vaNode.get("icon")),
                mapChannels(vaNode.get("bankChannels")),
                PendanaanJsonSupport.requireBoolean(vaNode.get("defaultFlag"), "defaultFlag"),
                PendanaanJsonSupport.requireBoolean(vaNode.get("disabled"), "disabled"),
                PendanaanJsonSupport.requireBoolean(vaNode.get("show"), "show")
        );
    }

    private static List<LenderRepayVa.LenderRepayVaChannel> mapChannels(JsonNode channelsNode) {
        if (channelsNode == null || channelsNode.isNull()) {
            return List.of();
        }
        if (!channelsNode.isArray()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderRepayVa.LenderRepayVaChannel> channels = new ArrayList<>();
        for (JsonNode channelNode : channelsNode) {
            channels.add(new LenderRepayVa.LenderRepayVaChannel(
                    PendanaanJsonSupport.textOrNull(channelNode.get("bankChannel")),
                    PendanaanJsonSupport.textOrNull(channelNode.get("instruction")),
                    Boolean.TRUE.equals(PendanaanJsonSupport.booleanOrNull(channelNode.get("defaultChannel")))
            ));
        }
        return List.copyOf(channels);
    }
}
