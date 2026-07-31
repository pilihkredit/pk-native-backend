package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayTrialFacade;
import java.math.BigDecimal;
import java.util.List;

public record RepayTrialBatchResponse(
        String batchTrialNo,
        BigDecimal totalShouldAmount,
        String totalShouldAmountDisplay,
        RepayTrialVaSummaryResponse defaultVa,
        List<RepayTrialBillSummaryResponse> billTrials
) {
    public static RepayTrialBatchResponse from(RepayTrialFacade.BatchTrialResult result) {
        return new RepayTrialBatchResponse(
                result.batchTrialNo(),
                result.totalShouldAmount(),
                result.totalShouldAmountDisplay(),
                result.defaultVa() == null ? null : RepayTrialVaSummaryResponse.from(result.defaultVa()),
                result.billTrials().stream().map(RepayTrialBillSummaryResponse::from).toList()
        );
    }
}
