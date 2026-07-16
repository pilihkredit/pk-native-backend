package com.pk.core.loan.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface LoanLenderStatusQueryRepository {
    void upsert(LoanLenderStatusQueryData data);

    Optional<LoanLenderStatusQueryData> findByLoanApplyId(String loanApplyId);

    Optional<LoanLenderStatusQueryData> findByLoanApplyIdAndProfileId(String loanApplyId, long profileId);

    record LoanLenderStatusQueryData(
            String loanApplyId,
            long profileId,
            String mobileNo,
            String lenderUserId,
            String externalLoanApplyNo,
            String externalStatus,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Instant payTime,
            Long freezeEndTime,
            String lastLenderRequestJson,
            String lastLenderResponseJson,
            Instant queriedAt
    ) {
    }
}
