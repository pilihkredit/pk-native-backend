package com.pk.core.repay.port;

import com.pk.core.repay.LenderRepayVa;
import java.util.List;

public interface LenderRepayVaPort {
    LenderRepayVaListResult listVas(String partnerUserId);

    void setDefaultVa(LenderRepayVaDefaultCommand command);

    record LenderRepayVaListResult(
            String partnerUserId,
            String userId,
            LenderRepayVa defaultVa,
            List<LenderRepayVa> vas,
            String rawResponseJson
    ) {
    }

    record LenderRepayVaDefaultCommand(
            String partnerUserId,
            String vaNo,
            String bankChannel
    ) {
    }
}
