package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayBillsOverviewFacade;
import java.math.BigDecimal;
import java.util.List;

public record RepayBillsOverviewResponse(
        int totalBillCount,
        BigDecimal totalDueAmount,
        String totalDueAmountDisplay,
        boolean hasOverdue,
        List<RepayBillSummaryResponse> bills
) {
    public static RepayBillsOverviewResponse from(RepayBillsOverviewFacade.OverviewResult result) {
        return new RepayBillsOverviewResponse(
                result.totalBillCount(),
                result.totalDueAmount(),
                result.totalDueAmountDisplay(),
                result.hasOverdue(),
                result.bills().stream().map(RepayBillSummaryResponse::from).toList()
        );
    }
}
