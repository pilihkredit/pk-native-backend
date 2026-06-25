package com.pk.core.credit.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface CreditLimitSnapshotRepository {
    void upsert(CreditLimitSnapshotData data);

    Optional<CreditLimitSnapshotData> findByCreditApplicationId(long creditApplicationId);

    record CreditLimitSnapshotData(
            long creditApplicationId,
            BigDecimal riskMinLimit,
            BigDecimal riskMaxLimit,
            BigDecimal psychologicalCreditLimit,
            BigDecimal fakeCreditLimit,
            BigDecimal borrowAmtStepSize,
            Instant contractExpireAt,
            String source
    ) {
    }
}
