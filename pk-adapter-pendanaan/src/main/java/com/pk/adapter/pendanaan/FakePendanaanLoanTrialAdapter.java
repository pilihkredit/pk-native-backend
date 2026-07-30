package com.pk.adapter.pendanaan;

import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LenderLoanTrialPort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FakePendanaanLoanTrialAdapter implements LenderLoanTrialPort {
    public FakePendanaanLoanTrialAdapter(com.fasterxml.jackson.databind.ObjectMapper ignored) {
    }

    public FakePendanaanLoanTrialAdapter() {
    }

    @Override
    public LenderLoanTrialResult trial(LenderLoanTrialCommand command) {
        int loanTerm = "RP002".equals(command.repayMethod()) ? 2 : 6;
        return trial(command, loanTerm, 30, new BigDecimal("0.18"), new BigDecimal("0.97"));
    }

    LenderLoanTrialResult trial(
            LenderLoanTrialCommand command,
            int loanTerm,
            int termDays,
            BigDecimal comprehensiveRate,
            BigDecimal disbursementRate
    ) {
        BigDecimal applyAmt = command.applyAmt();
        BigDecimal payAmount = applyAmt.multiply(disbursementRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal schdAmount = applyAmt.multiply(BigDecimal.ONE.add(comprehensiveRate))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal interest = schdAmount.subtract(applyAmt);
        List<LenderTrialTerm> terms = buildTerms(loanTerm, termDays, applyAmt, schdAmount, interest);
        LoanTrialQuoteDetail quote = buildQuote(
                command, applyAmt, payAmount, schdAmount, interest, loanTerm, termDays
        );
        return new LenderLoanTrialResult(quote, terms, 1L);
    }

    private LoanTrialQuoteDetail buildQuote(
            LenderLoanTrialCommand command,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            BigDecimal schdAmount,
            BigDecimal interest,
            int loanTerm,
            int termDays
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
                (long) loanTerm * termDays,
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
                now + (long) termDays * 24 * 60 * 60 * 1000,
                now + (long) loanTerm * termDays * 24 * 60 * 60 * 1000,
                false
        );
    }

    private List<LenderTrialTerm> buildTerms(
            int loanTerm,
            int termDays,
            BigDecimal applyAmt,
            BigDecimal schdAmount,
            BigDecimal interest
    ) {
        BigDecimal principalEach = applyAmt.divide(BigDecimal.valueOf(loanTerm), 0, RoundingMode.HALF_UP);
        BigDecimal interestEach = interest.divide(BigDecimal.valueOf(loanTerm), 0, RoundingMode.HALF_UP);
        BigDecimal schdEach = schdAmount.divide(BigDecimal.valueOf(loanTerm), 0, RoundingMode.HALF_UP);
        long baseValue = Instant.now().toEpochMilli();
        long dayMs = 24L * 60 * 60 * 1000;
        List<LenderTrialTerm> terms = new ArrayList<>();
        for (int termNo = 1; termNo <= loanTerm; termNo++) {
            long valueDate = baseValue + (termNo - 1L) * termDays * dayMs;
            long dueDate = baseValue + (long) termNo * termDays * dayMs;
            long graceDate = dueDate + 7L * dayMs;
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
}
