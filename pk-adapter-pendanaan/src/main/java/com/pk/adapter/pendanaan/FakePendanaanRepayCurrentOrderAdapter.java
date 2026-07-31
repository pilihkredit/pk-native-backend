package com.pk.adapter.pendanaan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;

public class FakePendanaanRepayCurrentOrderAdapter implements LenderRepayCurrentOrderPort {
    @Override
    public void setCurrentOrder(LenderRepayCurrentOrderCommand command) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
