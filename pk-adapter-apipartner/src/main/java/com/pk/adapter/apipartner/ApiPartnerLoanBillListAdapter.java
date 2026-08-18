package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.port.LenderLoanBillListPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ApiPartnerLoanBillListAdapter implements LenderLoanBillListPort {
    static final String BILL_LIST_PATH = ApiPartnerOpenApiPaths.LOAN_BILL_LIST;
    static final String BUSINESS_TYPE = "LOAN_BILL_LIST";

    private final ApiPartnerHttpClient httpClient;

    public ApiPartnerLoanBillListAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
    }

    @Override
    public LenderLoanBillListResult listBills(String partnerUserId, List<String> billStatuses) {
        String requestBody = ApiPartnerLoanRequestMapper.buildBillListBody(partnerUserId, billStatuses);
        ApiPartnerHttpClient.ExchangeResult exchange =
                httpClient.postWithInteraction(BILL_LIST_PATH, requestBody, BUSINESS_TYPE, partnerUserId);
        JsonNode data = exchange.data();
        List<LenderLoanBill> bills = new ArrayList<>();
        if (data != null && data.isArray()) {
            for (JsonNode item : data) {
                bills.add(toBill(item));
            }
        }
        return new LenderLoanBillListResult(exchange.interactionId(), bills);
    }

    private LenderLoanBill toBill(JsonNode item) {
        return new LenderLoanBill(
                textOrNull(item.get("loanApplyId")),
                textOrNull(item.get("loanApplyNo")),
                textOrNull(item.get("userId")),
                textOrNull(item.get("billNo")),
                textOrNull(item.get("contrNo")),
                intOrNull(item.get("terms")),
                decimalOrNull(item.get("applyAmt")),
                textOrNull(item.get("currency")),
                textOrNull(item.get("userName")),
                longOrNull(item.get("statusDate")),
                textOrNull(item.get("billStatus")),
                decimalOrNull(item.get("feePrepayAmt")),
                decimalOrNull(item.get("lendAmt")),
                longOrNull(item.get("lendTime")),
                textOrNull(item.get("paySerialNo")),
                longOrNull(item.get("dueDate")),
                decimalOrNull(item.get("stampDuty")),
                decimalOrNull(item.get("principal")),
                decimalOrNull(item.get("interest")),
                decimalOrNull(item.get("fee1")),
                decimalOrNull(item.get("fee2")),
                decimalOrNull(item.get("fee3")),
                decimalOrNull(item.get("fee1Tax")),
                decimalOrNull(item.get("fee2Tax")),
                decimalOrNull(item.get("fee3Tax")),
                decimalOrNull(item.get("prePenInterest")),
                decimalOrNull(item.get("penInterest")),
                decimalOrNull(item.get("initLateFee")),
                intOrNull(item.get("termNo")),
                longOrNull(item.get("termDueDate")),
                decimalOrNull(item.get("shouldStampDuty")),
                decimalOrNull(item.get("shouldPrincipal")),
                decimalOrNull(item.get("shouldInterest")),
                decimalOrNull(item.get("shouldFee1")),
                decimalOrNull(item.get("shouldFee2")),
                decimalOrNull(item.get("shouldFee3")),
                decimalOrNull(item.get("shouldFee1Tax")),
                decimalOrNull(item.get("shouldFee2Tax")),
                decimalOrNull(item.get("shouldFee3Tax")),
                decimalOrNull(item.get("shouldPenInterest")),
                decimalOrNull(item.get("shouldInitLateFee")),
                decimalOrNull(item.get("paidStampDuty")),
                decimalOrNull(item.get("paidPrincipal")),
                decimalOrNull(item.get("paidInterest")),
                decimalOrNull(item.get("paidFee1")),
                decimalOrNull(item.get("paidFee2")),
                decimalOrNull(item.get("paidFee3")),
                decimalOrNull(item.get("paidFee1Tax")),
                decimalOrNull(item.get("paidFee2Tax")),
                decimalOrNull(item.get("paidFee3Tax")),
                decimalOrNull(item.get("paidPenInterest")),
                decimalOrNull(item.get("paidInitLateFee")),
                decimalOrNull(item.get("paidAdvSettleFee")),
                decimalOrNull(item.get("reductionStampDuty")),
                decimalOrNull(item.get("reductionPrincipal")),
                decimalOrNull(item.get("reductionInterest")),
                decimalOrNull(item.get("reductionFee1")),
                decimalOrNull(item.get("reductionFee2")),
                decimalOrNull(item.get("reductionFee3")),
                decimalOrNull(item.get("reductionFee1Tax")),
                decimalOrNull(item.get("reductionFee2Tax")),
                decimalOrNull(item.get("reductionFee3Tax")),
                decimalOrNull(item.get("reductionPenInterest")),
                decimalOrNull(item.get("reductionInitLateFee")),
                decimalOrNull(item.get("reductionAdvSettleFee")),
                intOrNull(item.get("overdueDays")),
                longOrNull(item.get("firstOverdueDay")),
                longOrNull(item.get("lastRepayTime")),
                intOrNull(item.get("maxOverdueDays")),
                longOrNull(item.get("paidOutDate")),
                textOrNull(item.get("advSetteFlag"))
        );
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }

    private static Long longOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asLong();
    }

    private static Integer intOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asInt();
    }

    private static BigDecimal decimalOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }
}
