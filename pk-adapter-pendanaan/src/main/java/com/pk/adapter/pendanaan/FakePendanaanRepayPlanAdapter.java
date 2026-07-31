package com.pk.adapter.pendanaan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.port.LenderRepayPlanPort;

public class FakePendanaanRepayPlanAdapter implements LenderRepayPlanPort {
    @Override
    public LenderRepayPlanResult fetchPlan(String loanApplyId) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
