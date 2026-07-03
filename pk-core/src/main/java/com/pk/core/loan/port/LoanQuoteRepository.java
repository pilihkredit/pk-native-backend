package com.pk.core.loan.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoanQuoteRepository {
    LoanQuoteRecord insert(LoanQuoteInsert command, List<LoanQuoteTermInsert> terms);

    Optional<LoanQuoteRecord> findByQuoteNo(String quoteNo);

    int countTermsByQuoteId(long quoteId);

    record LoanQuoteInsert(
            String quoteNo,
            long creditApplicationId,
            String mobileNo,
            Long productSnapshotId,
            String productCode,
            String repayMethod,
            BigDecimal applyAmt,
            BigDecimal loanPrincipal,
            BigDecimal payAmount,
            BigDecimal schdAmount,
            BigDecimal interest,
            Integer totalDays,
            String feeJson,
            String lastLenderRequestJson,
            String lastLenderResponseJson,
            String rawResponseJson,
            Instant quotedAt
    ) {
    }

    record LoanQuoteTermInsert(
            String mobileNo,
            int termNo,
            Instant dueDate,
            BigDecimal schdAmount,
            BigDecimal schdPrincipal,
            BigDecimal schdInterest,
            String feeJson
    ) {
    }

    record LoanQuoteRecord(
            long id,
            String quoteNo,
            long creditApplicationId,
            String mobileNo,
            Long productSnapshotId,
            String productCode,
            String repayMethod,
            BigDecimal applyAmt,
            BigDecimal loanPrincipal,
            BigDecimal payAmount,
            BigDecimal schdAmount,
            BigDecimal interest,
            Integer totalDays,
            String feeJson,
            String lastLenderRequestJson,
            String lastLenderResponseJson,
            String rawResponseJson,
            Instant quotedAt
    ) {
    }
}
