package com.pk.core.home.port;

import com.pk.core.profile.sync.LenderDeviceContext;

public interface LenderUserStatusPort {
    LenderUserStatusResult queryStatus(LenderUserStatusCommand command);

    record LenderUserStatusCommand(
            String partnerUserId,
            LenderDeviceContext device
    ) {
    }

    record LenderUserStatusResult(
            String partnerUserId,
            String userId,
            Integer userLoanLifeTimeStatus,
            Integer userLoanLifeTimeLastAction,
            Long freezeEndTime,
            Integer onLoanCount,
            Long creditContractExpireTime,
            String requestJson,
            String responseDataJson
    ) {
    }
}
