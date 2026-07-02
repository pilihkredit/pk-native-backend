package com.pk.core.credit.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface CreditLenderStatusQueryRepository {
    void upsert(CreditLenderStatusQueryData data);

    Optional<CreditLenderStatusQueryData> findByApplyId(String applyId);

    Optional<CreditLenderStatusQueryData> findByApplyIdAndProfileId(String applyId, long profileId);

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
            String lastLenderRequestJson,
            String lastLenderResponseJson,
            Instant queriedAt
    ) {
    }
}
