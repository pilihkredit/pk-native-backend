package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.loan.port.LenderLoanApplyPort;
import java.math.BigDecimal;
import java.util.List;

final class ApiPartnerLoanRequestMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ApiPartnerLoanRequestMapper() {
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
            riskDataInfo.set("openUserDevice", ApiPartnerDeviceNodeBuilder.buildRiskApplyDevice(command.device()));
            if (!isIos(command.device().systemPlatform())) {
                riskDataInfo.set("appList", buildAppList(command.appList()));
            }
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

    static String buildHistoryListBody(String partnerUserId) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("partnerUserId", partnerUserId);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build loan history list request", exception);
        }
    }

    static String buildBillListBody(String partnerUserId, List<String> billStatuses) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("partnerUserId", partnerUserId);
            if (billStatuses != null && !billStatuses.isEmpty()) {
                ArrayNode array = root.putArray("billStatus");
                for (String billStatus : billStatuses) {
                    array.add(billStatus);
                }
            }
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build loan bill list request", exception);
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

    private static boolean isIos(String systemPlatform) {
        return systemPlatform != null && "ios".equalsIgnoreCase(systemPlatform.trim());
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
