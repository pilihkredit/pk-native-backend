package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.credit.port.LenderCreditPort;
import java.math.BigDecimal;
import java.util.List;

final class PendanaanCreditRequestMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PendanaanCreditRequestMapper() {
    }

    static String buildApplyBody(LenderCreditPort.LenderCreditApplyCommand command) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("applyId", command.applyId());
            root.put("partnerUserId", command.partnerUserId());
            putDecimal(root, "lat", command.lat());
            putDecimal(root, "lng", command.lng());
            if (command.ip() != null && !command.ip().isBlank()) {
                root.put("ip", command.ip().trim());
            }
            if (command.address() != null && !command.address().isBlank()) {
                root.put("address", command.address().trim());
            }
            if (command.device().adId() != null && !command.device().adId().isBlank()) {
                root.put("adId", command.device().adId().trim());
            }
            ObjectNode riskDataInfo = root.putObject("riskDataInfo");
            riskDataInfo.set("openUserDevice", PendanaanDeviceNodeBuilder.buildRiskApplyDevice(command.device()));
            riskDataInfo.set("appList", buildAppList(command.appList()));
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build credit apply request", exception);
        }
    }

    static String buildStatusBody(String applyId) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("applyId", applyId);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build credit status request", exception);
        }
    }

    private static ArrayNode buildAppList(List<CreditRiskAppInfo> appList) {
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        for (CreditRiskAppInfo appInfo : appList) {
            ObjectNode item = arrayNode.addObject();
            putIfPresent(item, "appName", appInfo.appName());
            putIfPresent(item, "packageName", appInfo.packageName());
            if (appInfo.appFlags() != null) {
                item.put("appFlags", appInfo.appFlags());
            }
            if (appInfo.appType() != null) {
                item.put("appType", appInfo.appType());
            }
            putIfPresent(item, "versionCode", appInfo.versionCode());
            putIfPresent(item, "versionName", appInfo.versionName());
            if (appInfo.inTime() != null) {
                item.put("inTime", appInfo.inTime());
            }
            if (appInfo.upTime() != null) {
                item.put("upTime", appInfo.upTime());
            }
        }
        return arrayNode;
    }

    private static void putDecimal(ObjectNode node, String field, BigDecimal value) {
        if (value != null) {
            node.put(field, value);
        }
    }

    private static void putIfPresent(ObjectNode node, String field, String value) {
        if (value != null && !value.isBlank()) {
            node.put(field, value.trim());
        }
    }
}
