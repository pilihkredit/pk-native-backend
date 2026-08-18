package com.pk.adapter.apipartner;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.port.LenderRepayPlanPort;

public class FakeApiPartnerRepayPlanAdapter implements LenderRepayPlanPort {
    @Override
    public LenderRepayPlanResult fetchPlan(String loanApplyId) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
