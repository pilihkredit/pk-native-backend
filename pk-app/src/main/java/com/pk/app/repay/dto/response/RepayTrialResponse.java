package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayTrialFacade;
import java.math.BigDecimal;

public record RepayTrialResponse(
        String trialNo,
        long expiresAt,
        BigDecimal repayAmount,
        String repayAmountDisplay,
        BigDecimal principal,
        String principalDisplay,
        BigDecimal interest,
        String interestDisplay,
        BigDecimal penalty,
        String penaltyDisplay,
        BigDecimal fee,
        String feeDisplay
) {
    public static RepayTrialResponse from(RepayTrialFacade.TrialResult result) {
        return new RepayTrialResponse(
                result.trialNo(),
                result.expiresAt(),
                result.repayAmount(),
                result.repayAmountDisplay(),
                result.principal(),
                result.principalDisplay(),
                result.interest(),
                result.interestDisplay(),
                result.penalty(),
                result.penaltyDisplay(),
                result.fee(),
                result.feeDisplay()
        );
    }
}
