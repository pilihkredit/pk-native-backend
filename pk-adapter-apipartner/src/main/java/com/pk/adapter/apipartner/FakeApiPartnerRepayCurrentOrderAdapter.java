package com.pk.adapter.apipartner;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;

public class FakeApiPartnerRepayCurrentOrderAdapter implements LenderRepayCurrentOrderPort {
    @Override
    public void setCurrentOrder(LenderRepayCurrentOrderCommand command) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
