package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.loan.LenderTrialTerm;
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
        String requestJson = buildRequestJson(command);
        String rawResponseJson = buildRawJson(command, payAmount, schdAmount, interest, loanTerm, terms);
        return new LenderLoanTrialResult(
                applyAmt,
                payAmount,
                schdAmount,
                interest,
                loanTerm,
                payAmount,
                loanTerm * 30,
                terms,
                requestJson,
                rawResponseJson
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
        Instant baseDue = Instant.now().plus(30, ChronoUnit.DAYS);
        List<LenderTrialTerm> terms = new ArrayList<>();
        for (int termNo = 1; termNo <= loanTerm; termNo++) {
            terms.add(new LenderTrialTerm(
                    termNo,
                    baseDue.plus((termNo - 1L) * 30L, ChronoUnit.DAYS),
                    schdEach,
                    principalEach,
                    interestEach
            ));
        }
        return List.copyOf(terms);
    }

    private String buildRawJson(
            LenderLoanTrialCommand command,
            BigDecimal payAmount,
            BigDecimal schdAmount,
            BigDecimal interest,
            int loanTerm,
            List<LenderTrialTerm> terms
    ) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("applyId", command.applyId());
            root.put("applyAmt", command.applyAmt());
            root.put("productCode", command.productCode());
            root.put("repayMethod", command.repayMethod());
            root.put("payAmount", payAmount);
            root.put("schdAmount", schdAmount);
            root.put("interest", interest);
            root.put("loanTerm", loanTerm);
            var termArray = objectMapper.createArrayNode();
            for (LenderTrialTerm term : terms) {
                var termNode = objectMapper.createObjectNode();
                termNode.put("termNo", term.termNo());
                if (term.dueDate() != null) {
                    termNode.put("dueDate", term.dueDate().toEpochMilli());
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
