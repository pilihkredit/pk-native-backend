package com.pk.app.loan.dto.response;

import com.pk.infra.loan.LoanProductFacade;
import java.util.List;

public record RepayMethodResponse(
        String repayMethod,
        String cycleType,
        Integer cycleInterval,
        Integer cycleCount,
        Integer totalCycleInterval,
        Integer repayMethodType,
        boolean repaymentUniform,
        List<UnevenBillRateResponse> unevenBillsRepaymentRates
) {
    public static RepayMethodResponse from(LoanProductFacade.RepayMethodResult result) {
        return new RepayMethodResponse(
                result.repayMethod(),
                result.cycleType(),
                result.cycleInterval(),
                result.cycleCount(),
                result.totalCycleInterval(),
                result.repayMethodType(),
                result.repaymentUniform(),
                result.unevenBillsRepaymentRates().stream()
                        .map(rate -> new UnevenBillRateResponse(rate.termNum(), rate.repaymentRate()))
                        .toList()
        );
    }
}
