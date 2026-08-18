package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.LenderRepayTrialTerm;
import com.pk.core.repay.port.LenderRepayTrialPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ApiPartnerRepayTrialAdapter implements LenderRepayTrialPort {
    static final String REPAY_TRIAL_PATH = ApiPartnerOpenApiPaths.REPAY_TRIAL;
    static final String REPAY_TRIAL_BATCH_PATH = ApiPartnerOpenApiPaths.REPAY_TRIAL_BATCH;
    static final String BUSINESS_TYPE_TRIAL = "REPAY_TRIAL";
    static final String BUSINESS_TYPE_TRIAL_BATCH = "REPAY_TRIAL_BATCH";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerRepayTrialAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderRepayTrialResult trial(LenderRepayTrialCommand command) {
        ApiPartnerHttpClient.ExchangeResult exchange = httpClient.postWithInteraction(
                REPAY_TRIAL_PATH,
                buildTrialRequestBody(command),
                BUSINESS_TYPE_TRIAL,
                command.loanApplyId()
        );
        return mapTrialResult(exchange.data(), exchange.interactionId());
    }

    @Override
    public LenderRepayTrialBatchResult trialBatch(LenderRepayTrialBatchCommand command) {
        ApiPartnerHttpClient.ExchangeResult exchange = httpClient.postWithInteraction(
                REPAY_TRIAL_BATCH_PATH,
                buildBatchRequestBody(command),
                BUSINESS_TYPE_TRIAL_BATCH,
                command.repayOrders().isEmpty() ? null : command.repayOrders().getFirst().loanApplyId()
        );
        JsonNode data = exchange.data();
        JsonNode billTrialsNode = data.get("billTrials");
        if (billTrialsNode == null || !billTrialsNode.isArray()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderRepayTrialResult> billTrials = new ArrayList<>();
        for (JsonNode trialNode : billTrialsNode) {
            billTrials.add(mapTrialResult(trialNode, exchange.interactionId()));
        }
        JsonNode defaultVaNode = data.get("defaultVa");
        return new LenderRepayTrialBatchResult(
                ApiPartnerJsonSupport.requireInt(data.get("totalBillCount"), "totalBillCount"),
                ApiPartnerJsonSupport.requireDecimal(data.get("totalShouldAmount"), "totalShouldAmount"),
                ApiPartnerJsonSupport.requireDecimal(data.get("totalReductionAmount"), "totalReductionAmount"),
                ApiPartnerJsonSupport.requireDecimal(data.get("totalPaidAmount"), "totalPaidAmount"),
                defaultVaNode == null || defaultVaNode.isNull() ? null : ApiPartnerRepayVaAdapter.mapVa(defaultVaNode),
                mapOptionalVa(data.get("spareVa")),
                mapOptionalVa(data.get("disabledDefaultVa")),
                List.copyOf(billTrials),
                exchange.interactionId()
        );
    }

    private String buildTrialRequestBody(LenderRepayTrialCommand command) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("loanApplyId", command.loanApplyId());
            root.put("settle", command.settle());
            ArrayNode termNos = root.putArray("termNos");
            if (command.termNos() != null) {
                command.termNos().forEach(termNos::add);
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private String buildBatchRequestBody(LenderRepayTrialBatchCommand command) {
        try {
            var root = objectMapper.createObjectNode();
            ArrayNode repayOrders = root.putArray("repayOrders");
            for (LenderRepayTrialCommand order : command.repayOrders()) {
                var orderNode = repayOrders.addObject();
                orderNode.put("loanApplyId", order.loanApplyId());
                orderNode.put("settle", order.settle());
                ArrayNode termNos = orderNode.putArray("termNos");
                if (order.termNos() != null) {
                    order.termNos().forEach(termNos::add);
                }
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private LenderRepayTrialResult mapTrialResult(JsonNode data, Long externalInteractionId) {
        if (data == null || data.isNull()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        JsonNode defaultVaNode = data.get("defaultVa");
        return new LenderRepayTrialResult(
                ApiPartnerJsonSupport.requireText(data.get("loanApplyId"), "loanApplyId"),
                ApiPartnerJsonSupport.requireText(data.get("loanApplyNo"), "loanApplyNo"),
                ApiPartnerJsonSupport.requireText(data.get("billNo"), "billNo"),
                ApiPartnerJsonSupport.requireText(data.get("billStatus"), "billStatus"),
                ApiPartnerJsonSupport.textOrNull(data.get("name")),
                ApiPartnerJsonSupport.textOrNull(data.get("userId")),
                ApiPartnerJsonSupport.textOrNull(data.get("advSetteFlag")),
                ApiPartnerJsonSupport.textOrNull(data.get("currency")),
                ApiPartnerJsonSupport.requireDecimal(data.get("applyAmt"), "applyAmt"),
                millisToInstant(ApiPartnerJsonSupport.longOrNull(data.get("applyTime"))),
                millisToInstant(ApiPartnerJsonSupport.longOrNull(data.get("auditTime"))),
                millisToInstant(ApiPartnerJsonSupport.longOrNull(data.get("loanTime"))),
                ApiPartnerJsonSupport.intOrNull(data.get("loanDays")),
                ApiPartnerJsonSupport.intOrNull(data.get("repayMethodType")),
                ApiPartnerJsonSupport.requireDecimal(data.get("schdAmount"), "schdAmount"),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdStampDuty")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdPrincipal")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdInterest")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdFee1")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdFee2")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdFee3")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdFee1Tax")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdFee2Tax")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("schdFee3Tax")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("prePenInterest")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("penInterest")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("initLateFee")),
                ApiPartnerJsonSupport.requireDecimal(data.get("shouldAmount"), "shouldAmount"),
                ApiPartnerJsonSupport.decimalOrNull(data.get("shouldStampDuty")),
                ApiPartnerJsonSupport.requireDecimal(data.get("shouldPrincipal"), "shouldPrincipal"),
                ApiPartnerJsonSupport.requireDecimal(data.get("shouldInterest"), "shouldInterest"),
                ApiPartnerJsonSupport.textOrNull(data.get("fee1Name")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("shouldFee1")),
                ApiPartnerJsonSupport.textOrNull(data.get("fee2Name")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("shouldFee2")),
                ApiPartnerJsonSupport.textOrNull(data.get("fee3Name")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("shouldFee3")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("shouldFee1Tax")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("shouldFee2Tax")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("shouldFee3Tax")),
                ApiPartnerJsonSupport.requireDecimal(data.get("shouldFee"), "shouldFee"),
                ApiPartnerJsonSupport.requireDecimal(data.get("shouldPenInterest"), "shouldPenInterest"),
                ApiPartnerJsonSupport.requireDecimal(data.get("shouldInitLateFee"), "shouldInitLateFee"),
                ApiPartnerJsonSupport.requireDecimal(data.get("shouldAdvSettleFee"), "shouldAdvSettleFee"),
                ApiPartnerJsonSupport.requireDecimal(data.get("shouldPenalty"), "shouldPenalty"),
                ApiPartnerJsonSupport.requireDecimal(data.get("reductionAmount"), "reductionAmount"),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionStampDuty")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionPrincipal")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionInterest")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionFee1")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionFee2")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionFee3")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionFee1Tax")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionFee2Tax")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionFee3Tax")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionPenInterest")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("reductionInitLateFee")),
                ApiPartnerJsonSupport.decimalOrNull(data.get("couponAmount")),
                ApiPartnerJsonSupport.textOrNull(data.get("partRepayFlag")),
                ApiPartnerJsonSupport.requireDecimal(data.get("paidAmount"), "paidAmount"),
                defaultVaNode == null || defaultVaNode.isNull() ? null : ApiPartnerRepayVaAdapter.mapVa(defaultVaNode),
                mapOptionalVa(data.get("spareVa")),
                mapOptionalVa(data.get("disabledDefaultVa")),
                ApiPartnerJsonSupport.longOrNull(data.get("couponId")),
                ApiPartnerJsonSupport.textOrNull(data.get("couponType")),
                ApiPartnerJsonSupport.textOrNull(data.get("couponName")),
                ApiPartnerJsonSupport.longOrNull(data.get("userValideDisTime")),
                ApiPartnerJsonSupport.intOrNull(data.get("termNo")),
                millisToInstant(ApiPartnerJsonSupport.longOrNull(data.get("termDueDate"))),
                mapTrialTerms(data.get("termInfo")),
                externalInteractionId
        );
    }

    private List<LenderRepayTrialTerm> mapTrialTerms(JsonNode termInfoNode) {
        if (termInfoNode == null || !termInfoNode.isArray()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderRepayTrialTerm> terms = new ArrayList<>();
        for (JsonNode termNode : termInfoNode) {
            int termNo = ApiPartnerJsonSupport.requireInt(termNode.get("termNo"), "termNo");
            terms.add(new LenderRepayTrialTerm(
                    ApiPartnerJsonSupport.requireText(termNode.get("billNo"), "billNo"),
                    ApiPartnerJsonSupport.requireText(termNode.get("loanApplyNo"), "loanApplyNo"),
                    termNo,
                    ApiPartnerJsonSupport.textOrNull(termNode.get("advSetteFlag")),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("schdAmount"), "schdAmount"),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdStampDuty")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdPrincipal")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdInterest")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdAllFee")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdAllTaxFee")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdFee1")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdFee2")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdFee3")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdFee1Tax")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdFee2Tax")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("schdFee3Tax")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("prePenInterest")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("penInterest")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("initLateFee")),
                    millisToInstant(ApiPartnerJsonSupport.requireLong(termNode.get("dueDate"), "dueDate")),
                    ApiPartnerJsonSupport.requireInt(termNode.get("overdueDays"), "overdueDays"),
                    millisToInstant(ApiPartnerJsonSupport.longOrNull(termNode.get("graceDate"))),
                    ApiPartnerJsonSupport.requireText(termNode.get("termStatus"), "termStatus"),
                    ApiPartnerJsonSupport.longOrNull(termNode.get("daysOfDueDate")),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("shouldAmount"), "shouldAmount"),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("shouldStampDuty")),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("shouldPrincipal"), "shouldPrincipal"),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("shouldInterest"), "shouldInterest"),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("shouldFee1")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("shouldFee2")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("shouldFee3")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("shouldFee1Tax")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("shouldFee2Tax")),
                    ApiPartnerJsonSupport.decimalOrNull(termNode.get("shouldFee3Tax")),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("shouldPenInterest"), "shouldPenInterest"),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("shouldInitLateFee"), "shouldInitLateFee"),
                    ApiPartnerJsonSupport.textOrNull(termNode.get("partRepayFlag")),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("paidAmount"), "paidAmount"),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("couponDiscount"), "couponDiscount"),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("reductionAmount"), "reductionAmount"),
                    mapDiscountInfos(termNode.get("discountInfos"))
            ));
        }
        return List.copyOf(terms);
    }

    private List<com.pk.core.repay.LenderRepayTrialDiscountInfo> mapDiscountInfos(JsonNode discountInfosNode) {
        if (discountInfosNode == null || discountInfosNode.isNull() || !discountInfosNode.isArray()) {
            return List.of();
        }
        List<com.pk.core.repay.LenderRepayTrialDiscountInfo> discounts = new ArrayList<>();
        for (JsonNode node : discountInfosNode) {
            discounts.add(new com.pk.core.repay.LenderRepayTrialDiscountInfo(
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionAllAmt")),
                    ApiPartnerJsonSupport.textOrNull(node.get("reductionType")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionStampDuty")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionPrincipal")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionInterest")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionFee1")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionFee2")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionFee3")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionFee1Tax")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionFee2Tax")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionFee3Tax")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionPenInterest")),
                    ApiPartnerJsonSupport.decimalOrNull(node.get("reductionInitLateFee")),
                    ApiPartnerJsonSupport.longOrNull(node.get("couponId")),
                    ApiPartnerJsonSupport.textOrNull(node.get("couponType"))
            ));
        }
        return List.copyOf(discounts);
    }

    private static com.pk.core.repay.LenderRepayVa mapOptionalVa(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return ApiPartnerRepayVaAdapter.mapVa(node);
    }

    private static Instant millisToInstant(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis);
    }
}
