package com.pk.core.loan.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface LoanLenderStatusQueryRepository {
    void insert(LoanLenderStatusQueryData data);

    Optional<LoanLenderStatusQueryData> findLatestByLoanApplyId(String loanApplyId);

    Optional<LoanLenderStatusQueryData> findLatestByLoanApplyIdAndProfileId(String loanApplyId, long profileId);

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
            Long externalInteractionCallbackId,
            Instant queriedAt
    ) {
    }
}
