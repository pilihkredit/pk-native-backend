package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LenderLoanTrialPort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class FakePendanaanLoanTrialAdapter implements LenderLoanTrialPort {
    private final ObjectMapper objectMapper;

    public FakePendanaanLoanTrialAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderLoanTrialResult trial(LenderLoanTrialCommand command) {
        int loanTerm = "RP002".equals(command.repayMethod()) ? 2 : 6;
        BigDecimal applyAmt = command.applyAmt();
        BigDecimal payAmount = applyAmt.multiply(new BigDecimal("0.97")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal schdAmount = applyAmt.multiply(new BigDecimal("1.18")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal interest = schdAmount.subtract(applyAmt);
        List<LenderTrialTerm> terms = buildTerms(loanTerm, applyAmt, schdAmount, interest);
        LoanTrialQuoteDetail quote = buildQuote(command, applyAmt, payAmount, schdAmount, interest, loanTerm);
        String requestJson = buildRequestJson(command);
        String rawResponseJson = buildRawJson(quote, terms);
        return new LenderLoanTrialResult(quote, terms, requestJson, rawResponseJson);
    }

    private LoanTrialQuoteDetail buildQuote(
            LenderLoanTrialCommand command,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            BigDecimal schdAmount,
            BigDecimal interest,
            int loanTerm
    ) {
        long now = Instant.now().toEpochMilli();
        return new LoanTrialQuoteDetail(
                command.applyId(),
                "CA-FAKE-1",
                "USR-FAKE-1",
                applyAmt,
                command.productCode(),
                command.repayMethod(),
                loanTerm,
                applyAmt,
                applyAmt,
                payAmount,
                applyAmt.subtract(payAmount),
                schdAmount,
                schdAmount,
                interest,
                new BigDecimal("0.003"),
                new BigDecimal("0.003"),
                (long) loanTerm * 30,
                "Admin Fee",
                BigDecimal.ZERO,
                "Service Fee",
                BigDecimal.ZERO,
                "Insurance Fee",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "0.5",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                applyAmt,
                interest,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "N",
                0,
                now,
                now + 30L * 24 * 60 * 60 * 1000,
                now + (long) loanTerm * 30 * 24 * 60 * 60 * 1000,
                false
        );
    }

    private String buildRequestJson(LenderLoanTrialCommand command) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("applyId", command.applyId());
            root.put("applyAmt", command.applyAmt());
            root.put("productCode", command.productCode());
            root.put("repayMethod", command.repayMethod());
            if (command.couponId() != null) {
                root.put("couponId", command.couponId());
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build fake loan trial request JSON", exception);
        }
    }

    private List<LenderTrialTerm> buildTerms(
            int loanTerm,
            BigDecimal applyAmt,
            BigDecimal schdAmount,
            BigDecimal interest
    ) {
        BigDecimal principalEach = applyAmt.divide(BigDecimal.valueOf(loanTerm), 0, RoundingMode.HALF_UP);
        BigDecimal interestEach = interest.divide(BigDecimal.valueOf(loanTerm), 0, RoundingMode.HALF_UP);
        BigDecimal schdEach = schdAmount.divide(BigDecimal.valueOf(loanTerm), 0, RoundingMode.HALF_UP);
        Instant baseValue = Instant.now();
        Instant baseDue = Instant.now().plus(30, ChronoUnit.DAYS);
        List<LenderTrialTerm> terms = new ArrayList<>();
        for (int termNo = 1; termNo <= loanTerm; termNo++) {
            Instant valueDate = baseValue.plus((termNo - 1L) * 30L, ChronoUnit.DAYS);
            Instant dueDate = baseDue.plus((termNo - 1L) * 30L, ChronoUnit.DAYS);
            Instant graceDate = dueDate.plus(7, ChronoUnit.DAYS);
            terms.add(new LenderTrialTerm(
                    termNo,
                    valueDate,
                    dueDate,
                    graceDate,
                    schdEach,
                    principalEach,
                    principalEach,
                    interestEach,
                    interestEach,
                    schdEach,
                    principalEach,
                    interestEach,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            ));
        }
        return List.copyOf(terms);
    }

    private String buildRawJson(LoanTrialQuoteDetail quote, List<LenderTrialTerm> terms) {
        try {
            var root = (com.fasterxml.jackson.databind.node.ObjectNode) objectMapper.valueToTree(quote);
            var termArray = objectMapper.createArrayNode();
            for (LenderTrialTerm term : terms) {
                var termNode = objectMapper.createObjectNode();
                termNode.put("termNo", term.termNo());
                if (term.valueDate() != null) {
                    termNode.put("valueDate", term.valueDate().toEpochMilli());
                }
                if (term.dueDate() != null) {
                    termNode.put("dueDate", term.dueDate().toEpochMilli());
                }
                if (term.graceDate() != null) {
                    termNode.put("graceDate", term.graceDate().toEpochMilli());
                }
                termNode.put("schdAmount", term.schdAmount());
                termNode.put("schdPrincipal", term.schdPrincipal());
                termNode.put("schdInterest", term.schdInterest());
                termArray.add(termNode);
            }
            root.set("termInfo", termArray);
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            return "{}";
        }
    }
}
