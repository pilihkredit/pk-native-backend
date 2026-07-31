package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class PendanaanLoanTrialParser {
    private PendanaanLoanTrialParser() {
    }

    static LoanTrialQuoteDetail mapQuote(JsonNode data) {
        if (data == null || data.isNull()) {
            return emptyQuote();
        }
        return new LoanTrialQuoteDetail(
                textOrNull(data.get("applyId")),
                textOrNull(data.get("creditApplyNo")),
                textOrNull(data.get("userId")),
                decimalOrNull(data.get("applyAmt")),
                textOrNull(data.get("productCode")),
                textOrNull(data.get("repayMethod")),
                intOrNull(data.get("loanTerm")),
                decimalOrNull(data.get("loanPrincipal")),
                decimalOrNull(data.get("showLoanPrincipal")),
                decimalOrNull(data.get("payAmount")),
                decimalOrNull(data.get("handFee")),
                decimalOrNull(data.get("schdAmount")),
                decimalOrNull(data.get("shouldAmount")),
                decimalOrNull(data.get("interest")),
                decimalOrNull(data.get("dayRate")),
                decimalOrNull(data.get("showDayRate")),
                longOrNull(data.get("totalDays")),
                textOrNull(data.get("fee1Name")),
                decimalOrNull(data.get("fee1")),
                textOrNull(data.get("fee2Name")),
                decimalOrNull(data.get("fee2")),
                textOrNull(data.get("fee3Name")),
                decimalOrNull(data.get("fee3")),
                decimalOrNull(data.get("tax")),
                textOrNull(data.get("taxRatePercent")),
                decimalOrNull(data.get("ppnAmount")),
                decimalOrNull(data.get("shouldStampDuty")),
                decimalOrNull(data.get("shouldPrincipal")),
                decimalOrNull(data.get("shouldInterest")),
                decimalOrNull(data.get("shouldFee1")),
                decimalOrNull(data.get("shouldFee2")),
                decimalOrNull(data.get("shouldFee3")),
                decimalOrNull(data.get("shouldFee1Tax")),
                decimalOrNull(data.get("shouldFee2Tax")),
                decimalOrNull(data.get("shouldFee3Tax")),
                decimalOrNull(data.get("reductionAmount")),
                decimalOrNull(data.get("adReductionAmt")),
                decimalOrNull(data.get("adReductionRatio")),
                longOrNull(data.get("adReductionEndDate")),
                decimalOrNull(data.get("reductionStampDuty")),
                decimalOrNull(data.get("reductionPrincipal")),
                decimalOrNull(data.get("reductionInterest")),
                decimalOrNull(data.get("reductionFee1")),
                decimalOrNull(data.get("reductionFee2")),
                decimalOrNull(data.get("reductionFee3")),
                decimalOrNull(data.get("reductionFee1Tax")),
                decimalOrNull(data.get("reductionFee2Tax")),
                decimalOrNull(data.get("reductionFee3Tax")),
                textOrNull(data.get("afterServiceFeeStatus")),
                intOrNull(data.get("feePrepayType")),
                longOrNull(data.get("lendingDate")),
                longOrNull(data.get("firstRepayDate")),
                longOrNull(data.get("lastRepayDate")),
                booleanOrNull(data.get("unevenBillsFlag"))
        );
    }

    static List<LenderTrialTerm> mapTerms(JsonNode termInfoNode) {
        if (termInfoNode == null || !termInfoNode.isArray()) {
            return List.of();
        }
        List<LenderTrialTerm> terms = new ArrayList<>();
        for (JsonNode termNode : termInfoNode) {
            Integer termNo = intOrNull(termNode.get("termNo"));
            if (termNo == null) {
                continue;
            }
            terms.add(new LenderTrialTerm(
                    termNo,
                    longOrNull(termNode.get("valueDate")),
                    longOrNull(termNode.get("dueDate")),
                    longOrNull(termNode.get("graceDate")),
                    decimalOrNull(termNode.get("schdAmount")),
                    decimalOrNull(termNode.get("schdPrincipal")),
                    decimalOrNull(termNode.get("showLoanPrincipal")),
                    decimalOrNull(termNode.get("schdInterest")),
                    decimalOrNull(termNode.get("showInterest")),
                    decimalOrNull(termNode.get("shouldAmount")),
                    decimalOrNull(termNode.get("shouldPrincipal")),
                    decimalOrNull(termNode.get("shouldInterest")),
                    decimalOrNull(termNode.get("fee1")),
                    decimalOrNull(termNode.get("fee2")),
                    decimalOrNull(termNode.get("fee3")),
                    decimalOrNull(termNode.get("fee1Tax")),
                    decimalOrNull(termNode.get("fee2Tax")),
                    decimalOrNull(termNode.get("fee3Tax")),
                    decimalOrNull(termNode.get("stampDuty")),
                    decimalOrNull(termNode.get("shouldStampDuty")),
                    decimalOrNull(termNode.get("reductionAmount")),
                    decimalOrNull(termNode.get("reductionPrincipal")),
                    decimalOrNull(termNode.get("reductionInterest")),
                    decimalOrNull(termNode.get("reductionFee1")),
                    decimalOrNull(termNode.get("reductionFee2")),
                    decimalOrNull(termNode.get("reductionFee3")),
                    decimalOrNull(termNode.get("reductionFee1Tax")),
                    decimalOrNull(termNode.get("reductionFee2Tax")),
                    decimalOrNull(termNode.get("reductionFee3Tax")),
                    decimalOrNull(termNode.get("reductionStampDuty"))
            ));
        }
        return List.copyOf(terms);
    }

    private static LoanTrialQuoteDetail emptyQuote() {
        return new LoanTrialQuoteDetail(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null
        );
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }

    private static Integer intOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asInt();
    }

    private static Long longOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asLong();
    }

    private static Boolean booleanOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asBoolean();
    }

    private static BigDecimal decimalOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }
}
