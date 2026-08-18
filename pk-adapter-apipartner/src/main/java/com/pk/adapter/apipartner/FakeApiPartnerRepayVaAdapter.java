package com.pk.adapter.apipartner;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.port.LenderRepayVaPort;

public class FakeApiPartnerRepayVaAdapter implements LenderRepayVaPort {
    @Override
    public LenderRepayVaListResult listVas(String partnerUserId) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }

    @Override
    public void setDefaultVa(LenderRepayVaDefaultCommand command) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
