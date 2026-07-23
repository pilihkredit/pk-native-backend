package com.pk.core.credit.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface CreditLenderStatusQueryRepository {
    void insert(CreditLenderStatusQueryData data);

    Optional<CreditLenderStatusQueryData> findLatestByApplyId(String applyId);

    Optional<CreditLenderStatusQueryData> findLatestByApplyIdAndProfileId(String applyId, long profileId);

    record CreditLenderStatusQueryData(
            String applyId,
            long profileId,
            String mobileNo,
            String partnerUserId,
            String lenderUserId,
            String creditApplyNo,
            String externalStatus,
            Long creditContractExpireTime,
            Long freezeEndTime,
            BigDecimal riskMinLimit,
            BigDecimal riskMaxLimit,
            BigDecimal psychologicalCreditLimit,
            BigDecimal fakeCreditLimit,
            BigDecimal borrowAmtStepSize,
            Long externalInteractionId,
            Long externalInteractionCallbackId,
            Instant queriedAt
    ) {
    }
}
