package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.loan.port.LenderLoanApplyPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.math.BigDecimal;
import java.util.List;

final class PendanaanLoanRequestMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PendanaanLoanRequestMapper() {
    }

    static String buildApplyBody(LenderLoanApplyPort.LenderLoanApplyCommand command) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("applyId", command.applyId());
            root.put("loanApplyId", command.loanApplyId());
            root.put("applyAmt", command.applyAmt());
            root.put("productCode", command.productCode());
            root.put("repayMethod", command.repayMethod());
            if (command.loanPurpose() != null && !command.loanPurpose().isBlank()) {
                root.put("loanPurpose", command.loanPurpose().trim());
            }
            if (command.couponId() != null) {
                root.put("couponId", command.couponId());
            }
            putDecimal(root, "lat", command.lat());
            putDecimal(root, "lng", command.lng());
            if (command.ip() != null && !command.ip().isBlank()) {
                root.put("ip", command.ip().trim());
            }
            if (command.address() != null && !command.address().isBlank()) {
                root.put("address", command.address().trim());
            }
            String adId = command.adId();
            if (adId == null && command.device() != null) {
                adId = command.device().adId();
            }
            if (adId != null && !adId.isBlank()) {
                root.put("adId", adId.trim());
            }
            ObjectNode riskDataInfo = root.putObject("riskDataInfo");
            riskDataInfo.set("openUserDevice", buildDeviceNode(command.device()));
            riskDataInfo.set("appList", buildAppList(command.appList()));
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build loan apply request", exception);
        }
    }

    static String buildStatusBody(String loanApplyId) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("loanApplyId", loanApplyId);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build loan status request", exception);
        }
    }

    static String buildContractListBody(String loanApplyId) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("loanApplyId", loanApplyId);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build loan contract list request", exception);
        }
    }

    private static ObjectNode buildDeviceNode(LenderDeviceContext device) {
        ObjectNode deviceNode = OBJECT_MAPPER.createObjectNode();
        deviceNode.put("appName", device.appName());
        deviceNode.put("appVersion", device.appVersion());
        deviceNode.put("packageName", device.packageName());
        deviceNode.put("deviceNo", device.deviceNo());
        deviceNode.put("systemPlatform", device.systemPlatform());
        if (device.adId() != null && !device.adId().isBlank()) {
            deviceNode.put("adId", device.adId());
        }
        if (device.deviceOtherInfo() == null || device.deviceOtherInfo().isEmpty()) {
            deviceNode.putObject("deviceOtherInfo");
        } else {
            deviceNode.set("deviceOtherInfo", OBJECT_MAPPER.valueToTree(device.deviceOtherInfo()));
        }
        return deviceNode;
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
