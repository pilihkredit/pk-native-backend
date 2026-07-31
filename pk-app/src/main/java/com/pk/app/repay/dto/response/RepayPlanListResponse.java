package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayPlanFacade;
import java.math.BigDecimal;
import java.util.List;

public record RepayPlanListResponse(
        String loanApplyId,
        String billNo,
        List<RepayPlanTermResponse> terms,
        List<RepayPlanResponse> plans
) {
    public static RepayPlanListResponse single(RepayPlanResponse plan) {
        return new RepayPlanListResponse(plan.loanApplyId(), plan.billNo(), plan.terms(), null);
    }

    public static RepayPlanListResponse multiple(List<RepayPlanResponse> plans) {
        return new RepayPlanListResponse(null, null, null, plans);
    }
}
