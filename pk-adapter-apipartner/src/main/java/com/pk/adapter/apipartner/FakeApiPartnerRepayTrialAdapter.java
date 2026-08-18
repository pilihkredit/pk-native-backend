package com.pk.adapter.apipartner;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.port.LenderRepayTrialPort;

public class FakeApiPartnerRepayTrialAdapter implements LenderRepayTrialPort {
    @Override
    public LenderRepayTrialResult trial(LenderRepayTrialCommand command) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }

    @Override
    public LenderRepayTrialBatchResult trialBatch(LenderRepayTrialBatchCommand command) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
