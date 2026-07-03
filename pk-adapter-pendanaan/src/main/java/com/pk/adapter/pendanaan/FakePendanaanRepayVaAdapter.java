package com.pk.adapter.pendanaan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.port.LenderRepayVaPort;

public class FakePendanaanRepayVaAdapter implements LenderRepayVaPort {
    @Override
    public LenderRepayVaListResult listVas(String partnerUserId) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }

    @Override
    public void setDefaultVa(LenderRepayVaDefaultCommand command) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
