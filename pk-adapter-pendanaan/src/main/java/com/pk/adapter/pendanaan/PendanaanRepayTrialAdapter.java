package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.LenderRepayTrialTerm;
import com.pk.core.repay.port.LenderRepayTrialPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PendanaanRepayTrialAdapter implements LenderRepayTrialPort {
    static final String REPAY_TRIAL_PATH = PendanaanOpenApiPaths.REPAY_TRIAL;
    static final String REPAY_TRIAL_BATCH_PATH = PendanaanOpenApiPaths.REPAY_TRIAL_BATCH;
    static final String BUSINESS_TYPE_TRIAL = "REPAY_TRIAL";
    static final String BUSINESS_TYPE_TRIAL_BATCH = "REPAY_TRIAL_BATCH";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanRepayTrialAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderRepayTrialResult trial(LenderRepayTrialCommand command) {
        JsonNode data = httpClient.post(
                REPAY_TRIAL_PATH,
                buildTrialRequestBody(command),
                BUSINESS_TYPE_TRIAL,
                command.loanApplyId()
        );
        return mapTrialResult(data);
    }

    @Override
    public LenderRepayTrialBatchResult trialBatch(LenderRepayTrialBatchCommand command) {
        JsonNode data = httpClient.post(
                REPAY_TRIAL_BATCH_PATH,
                buildBatchRequestBody(command),
                BUSINESS_TYPE_TRIAL_BATCH,
                command.repayOrders().isEmpty() ? null : command.repayOrders().getFirst().loanApplyId()
        );
        String rawResponseJson = data == null ? "{}" : data.toString();
        JsonNode billTrialsNode = data.get("billTrials");
        if (billTrialsNode == null || !billTrialsNode.isArray()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderRepayTrialResult> billTrials = new ArrayList<>();
        for (JsonNode trialNode : billTrialsNode) {
            billTrials.add(mapTrialResult(trialNode));
        }
        JsonNode defaultVaNode = data.get("defaultVa");
        return new LenderRepayTrialBatchResult(
                PendanaanJsonSupport.requireInt(data.get("totalBillCount"), "totalBillCount"),
                PendanaanJsonSupport.requireDecimal(data.get("totalShouldAmount"), "totalShouldAmount"),
                PendanaanJsonSupport.requireDecimal(data.get("totalReductionAmount"), "totalReductionAmount"),
                PendanaanJsonSupport.requireDecimal(data.get("totalPaidAmount"), "totalPaidAmount"),
                defaultVaNode == null || defaultVaNode.isNull() ? null : PendanaanRepayVaAdapter.mapVa(defaultVaNode),
                mapOptionalVa(data.get("spareVa")),
                mapOptionalVa(data.get("disabledDefaultVa")),
                List.copyOf(billTrials),
                rawResponseJson
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

    private LenderRepayTrialResult mapTrialResult(JsonNode data) {
        if (data == null || data.isNull()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        String rawResponseJson = data.toString();
        JsonNode defaultVaNode = data.get("defaultVa");
        return new LenderRepayTrialResult(
                PendanaanJsonSupport.requireText(data.get("loanApplyId"), "loanApplyId"),
                PendanaanJsonSupport.requireText(data.get("loanApplyNo"), "loanApplyNo"),
                PendanaanJsonSupport.requireText(data.get("billNo"), "billNo"),
                PendanaanJsonSupport.requireText(data.get("billStatus"), "billStatus"),
                PendanaanJsonSupport.textOrNull(data.get("name")),
                PendanaanJsonSupport.textOrNull(data.get("userId")),
                PendanaanJsonSupport.textOrNull(data.get("advSetteFlag")),
                PendanaanJsonSupport.textOrNull(data.get("currency")),
                PendanaanJsonSupport.requireDecimal(data.get("applyAmt"), "applyAmt"),
                millisToInstant(PendanaanJsonSupport.longOrNull(data.get("applyTime"))),
                millisToInstant(PendanaanJsonSupport.longOrNull(data.get("auditTime"))),
                millisToInstant(PendanaanJsonSupport.longOrNull(data.get("loanTime"))),
                PendanaanJsonSupport.intOrNull(data.get("loanDays")),
                PendanaanJsonSupport.intOrNull(data.get("repayMethodType")),
                PendanaanJsonSupport.requireDecimal(data.get("schdAmount"), "schdAmount"),
                PendanaanJsonSupport.requireDecimal(data.get("shouldAmount"), "shouldAmount"),
                PendanaanJsonSupport.requireDecimal(data.get("shouldPrincipal"), "shouldPrincipal"),
                PendanaanJsonSupport.requireDecimal(data.get("shouldInterest"), "shouldInterest"),
                PendanaanJsonSupport.requireDecimal(data.get("shouldFee"), "shouldFee"),
                PendanaanJsonSupport.requireDecimal(data.get("shouldPenInterest"), "shouldPenInterest"),
                PendanaanJsonSupport.requireDecimal(data.get("shouldInitLateFee"), "shouldInitLateFee"),
                PendanaanJsonSupport.requireDecimal(data.get("shouldPenalty"), "shouldPenalty"),
                PendanaanJsonSupport.requireDecimal(data.get("shouldAdvSettleFee"), "shouldAdvSettleFee"),
                PendanaanJsonSupport.requireDecimal(data.get("reductionAmount"), "reductionAmount"),
                PendanaanJsonSupport.requireDecimal(data.get("paidAmount"), "paidAmount"),
                defaultVaNode == null || defaultVaNode.isNull() ? null : PendanaanRepayVaAdapter.mapVa(defaultVaNode),
                mapOptionalVa(data.get("spareVa")),
                mapOptionalVa(data.get("disabledDefaultVa")),
                PendanaanJsonSupport.longOrNull(data.get("couponId")),
                PendanaanJsonSupport.textOrNull(data.get("couponType")),
                PendanaanJsonSupport.textOrNull(data.get("couponName")),
                PendanaanJsonSupport.longOrNull(data.get("userValideDisTime")),
                PendanaanJsonSupport.intOrNull(data.get("termNo")),
                millisToInstant(PendanaanJsonSupport.longOrNull(data.get("termDueDate"))),
                mapTrialTerms(data.get("termInfo")),
                rawResponseJson
        );
    }

    private List<LenderRepayTrialTerm> mapTrialTerms(JsonNode termInfoNode) {
        if (termInfoNode == null || !termInfoNode.isArray()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderRepayTrialTerm> terms = new ArrayList<>();
        for (JsonNode termNode : termInfoNode) {
            int termNo = PendanaanJsonSupport.requireInt(termNode.get("termNo"), "termNo");
            terms.add(new LenderRepayTrialTerm(
                    PendanaanJsonSupport.requireText(termNode.get("billNo"), "billNo"),
                    PendanaanJsonSupport.requireText(termNode.get("loanApplyNo"), "loanApplyNo"),
                    termNo,
                    PendanaanJsonSupport.textOrNull(termNode.get("advSetteFlag")),
                    PendanaanJsonSupport.requireDecimal(termNode.get("schdAmount"), "schdAmount"),
                    millisToInstant(PendanaanJsonSupport.requireLong(termNode.get("dueDate"), "dueDate")),
                    PendanaanJsonSupport.requireInt(termNode.get("overdueDays"), "overdueDays"),
                    millisToInstant(PendanaanJsonSupport.longOrNull(termNode.get("graceDate"))),
                    PendanaanJsonSupport.requireText(termNode.get("termStatus"), "termStatus"),
                    PendanaanJsonSupport.longOrNull(termNode.get("daysOfDueDate")),
                    PendanaanJsonSupport.requireDecimal(termNode.get("shouldAmount"), "shouldAmount"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("shouldPrincipal"), "shouldPrincipal"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("shouldInterest"), "shouldInterest"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("shouldPenInterest"), "shouldPenInterest"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("shouldInitLateFee"), "shouldInitLateFee"),
                    PendanaanJsonSupport.textOrNull(termNode.get("partRepayFlag")),
                    PendanaanJsonSupport.requireDecimal(termNode.get("paidAmount"), "paidAmount"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("couponDiscount"), "couponDiscount"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("reductionAmount"), "reductionAmount"),
                    termNode.toString()
            ));
        }
        return List.copyOf(terms);
    }

    private static com.pk.core.repay.LenderRepayVa mapOptionalVa(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return PendanaanRepayVaAdapter.mapVa(node);
    }

    private static Instant millisToInstant(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis);
    }
}
