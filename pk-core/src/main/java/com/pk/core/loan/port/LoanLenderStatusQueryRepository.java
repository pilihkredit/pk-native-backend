package com.pk.core.loan.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface LoanLenderStatusQueryRepository {
    void insert(LoanLenderStatusQueryData data);

    Optional<LoanLenderStatusQueryData> findLatestByLoanApplyId(String loanApplyId);

    Optional<LoanLenderStatusQueryData> findLatestByLoanApplyIdAndUserId(String loanApplyId, long userId);

    record LoanLenderStatusQueryData(
            String loanApplyId,
            long userId,
            String lenderUserId,
            String externalLoanApplyNo,
            String externalStatus,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Instant payTime,
            Long freezeEndTime,
            Long externalInteractionId,
            Long externalInteractionCallbackId,
            String source,
            Instant queriedAt
    ) {
    }
}
