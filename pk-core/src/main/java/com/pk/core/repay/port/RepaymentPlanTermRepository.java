package com.pk.core.repay.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface RepaymentPlanTermRepository {
    void upsertTerms(
            long loanApplicationId,
            String loanApplyId,
            String billNo,
            List<TermUpsert> terms,
            Long externalInteractionId,
            Instant syncedAt
    );

    List<TermRecord> findByLoanApplicationId(long loanApplicationId);

    List<TermRecord> findPendingByProfileId(long profileId);

    record TermUpsert(
            int termNo,
            String subBillNo,
            String termStatus,
            Instant dueDate,
            Instant graceDate,
            BigDecimal schdAmount,
            BigDecimal shouldAmount,
            BigDecimal paidAmount,
            Integer overdueDays,
            String amountDetailJson,
            Instant lastRepayTime
    ) {
    }

    record TermRecord(
            long id,
            long loanApplicationId,
            String loanApplyId,
            String billNo,
            String subBillNo,
            int termNo,
            String termStatus,
            Instant dueDate,
            Instant graceDate,
            BigDecimal schdAmount,
            BigDecimal shouldAmount,
            BigDecimal paidAmount,
            Integer overdueDays,
            String amountDetailJson,
            Instant lastRepayTime,
            Instant syncedAt
    ) {
    }
}
