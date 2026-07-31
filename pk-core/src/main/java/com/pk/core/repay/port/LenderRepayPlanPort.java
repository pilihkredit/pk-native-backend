package com.pk.core.repay.port;

import com.pk.core.repay.LenderRepayPlanTerm;
import java.util.List;

public interface LenderRepayPlanPort {
    LenderRepayPlanResult fetchPlan(String loanApplyId);

    record LenderRepayPlanResult(
            String loanApplyId,
            String loanApplyNo,
            String billNo,
            List<LenderRepayPlanTerm> terms,
            Long externalInteractionId
    ) {
    }
}
