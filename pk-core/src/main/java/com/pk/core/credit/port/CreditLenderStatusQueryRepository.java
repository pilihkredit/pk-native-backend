package com.pk.core.credit.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface CreditLenderStatusQueryRepository {
    void insert(CreditLenderStatusQueryData data);

    Optional<CreditLenderStatusQueryData> findLatestByApplyId(String applyId);

    Optional<CreditLenderStatusQueryData> findLatestByApplyIdAndUserId(String applyId, long userId);

    record CreditLenderStatusQueryData(
            String applyId,
            long userId,
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
            String source,
            Instant queriedAt
    ) {
    }
}
