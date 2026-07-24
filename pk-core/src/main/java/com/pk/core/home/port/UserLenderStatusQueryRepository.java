package com.pk.core.home.port;

import java.time.Instant;
import java.util.Optional;

public interface UserLenderStatusQueryRepository {
    void upsert(UserLenderStatusQueryData data);

    Optional<UserLenderStatusQueryData> findByProfileId(long profileId);

    record UserLenderStatusQueryData(
            long profileId,
            String mobileNo,
            String partnerUserId,
            String lenderUserId,
            Integer userLoanLifeTimeStatus,
            Integer userLoanLifeTimeLastAction,
            Long freezeEndTime,
            Boolean firstLoan,
            Boolean firstCreditApply,
            Boolean firstLoanApply,
            Integer onLoanCount,
            Long creditContractExpireTime,
            Boolean autoCredit,
            Long externalInteractionId,
            Instant queriedAt
    ) {
    }
}
