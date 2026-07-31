package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayPlanFacade;
import java.math.BigDecimal;
import java.util.List;

public record RepayPlanResponse(
        String loanApplyId,
        String billNo,
        List<RepayPlanTermResponse> terms
) {
    public static RepayPlanResponse from(RepayPlanFacade.PlanResult plan) {
        return new RepayPlanResponse(
                plan.loanApplyId(),
                plan.billNo(),
                plan.terms().stream().map(RepayPlanTermResponse::from).toList()
        );
    }
}
