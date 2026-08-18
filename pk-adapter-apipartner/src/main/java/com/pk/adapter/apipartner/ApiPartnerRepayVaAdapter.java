package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.LenderRepayVa;
import com.pk.core.repay.port.LenderRepayVaPort;
import java.util.ArrayList;
import java.util.List;

public class ApiPartnerRepayVaAdapter implements LenderRepayVaPort {
    static final String VA_LIST_PATH = ApiPartnerOpenApiPaths.REPAY_VA_LIST;
    static final String VA_DEFAULT_PATH = ApiPartnerOpenApiPaths.REPAY_VA_DEFAULT;
    static final String BUSINESS_TYPE_LIST = "REPAY_VA_LIST";
    static final String BUSINESS_TYPE_DEFAULT = "REPAY_VA_DEFAULT";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerRepayVaAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderRepayVaListResult listVas(String partnerUserId) {
        String requestBody = "{\"partnerUserId\":\"" + partnerUserId + "\"}";
        ApiPartnerHttpClient.ExchangeResult exchange =
                httpClient.postWithInteraction(VA_LIST_PATH, requestBody, BUSINESS_TYPE_LIST, partnerUserId);
        JsonNode data = exchange.data();
        JsonNode defaultVaNode = data.get("defaultVa");
        return new LenderRepayVaListResult(
                ApiPartnerJsonSupport.requireText(data.get("partnerUserId"), "partnerUserId"),
                ApiPartnerJsonSupport.requireText(data.get("userId"), "userId"),
                defaultVaNode == null || defaultVaNode.isNull() ? null : mapVa(defaultVaNode),
                mapVaList(data.get("vas")),
                exchange.interactionId()
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
                ApiPartnerJsonSupport.requireText(vaNode.get("vaNo"), "vaNo"),
                ApiPartnerJsonSupport.requireText(vaNode.get("bankCode"), "bankCode"),
                ApiPartnerJsonSupport.requireText(vaNode.get("bankName"), "bankName"),
                ApiPartnerJsonSupport.requireInt(vaNode.get("bankType"), "bankType"),
                ApiPartnerJsonSupport.textOrNull(vaNode.get("icon")),
                mapChannels(vaNode.get("bankChannels")),
                ApiPartnerJsonSupport.requireBoolean(vaNode.get("defaultFlag"), "defaultFlag"),
                ApiPartnerJsonSupport.requireBoolean(vaNode.get("disabled"), "disabled"),
                ApiPartnerJsonSupport.requireBoolean(vaNode.get("show"), "show")
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
                    ApiPartnerJsonSupport.textOrNull(channelNode.get("bankChannel")),
                    ApiPartnerJsonSupport.textOrNull(channelNode.get("instruction")),
                    Boolean.TRUE.equals(ApiPartnerJsonSupport.booleanOrNull(channelNode.get("defaultChannel")))
            ));
        }
        return List.copyOf(channels);
    }
}
