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
            Boolean firstLoan,
            Boolean firstCreditApply,
            Boolean firstLoanApply,
            Integer onLoanCount,
            Long creditContractExpireTime,
            Boolean autoCredit,
            String requestJson,
            String responseDataJson
    ) {
    }
}
