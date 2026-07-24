package com.pk.core.loan.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface LoanLenderHistoryOrderRepository {
    void upsert(LoanLenderHistoryOrderData data);

    List<LoanLenderHistoryOrderData> findByProfileId(long profileId);

    record LoanLenderHistoryOrderData(
            String loanApplyId,
            long profileId,
            String mobileNo,
            String externalLoanApplyNo,
            String lenderUserId,
            String externalStatus,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Instant payTime,
            Long freezeEndTime,
            Long lenderCreateTime,
            Long externalInteractionId,
            Instant queriedAt
    ) {
    }
}
