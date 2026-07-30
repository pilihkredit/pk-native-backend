package com.pk.core.review;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReviewSandboxLoanDataPort {
    Optional<ReviewLoanSnapshot> findByLoanApplyId(String loanApplyId);

    List<ReviewLoanSnapshot> findDisbursedByPartnerUserId(String partnerUserId);

    default boolean hasApprovedCredit(String mobileNo) {
        return false;
    }

    default boolean hasDisbursedLoan(String mobileNo) {
        return false;
    }

    record ReviewLoanSnapshot(
            String loanApplyId,
            String loanApplyNo,
            String partnerUserId,
            String lenderUserId,
            String billNo,
            BigDecimal applyAmount,
            BigDecimal payAmount,
            Instant payTime
    ) {
    }
}
