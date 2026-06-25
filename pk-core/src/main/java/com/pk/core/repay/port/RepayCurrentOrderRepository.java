package com.pk.core.repay.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RepayCurrentOrderRepository {
    CurrentOrderRecord upsertActive(CurrentOrderUpsert command);

    Optional<CurrentOrderRecord> findActiveByProfileId(long profileId);

    record CurrentOrderUpsert(
            long profileId,
            String currentOrderNo,
            long trialId,
            String repayOrdersJson,
            Long couponId,
            Instant submittedAt
    ) {
    }

    record CurrentOrderRecord(
            long id,
            long profileId,
            String currentOrderNo,
            long trialId,
            String repayOrdersJson,
            Long couponId,
            String status,
            Instant submittedAt
    ) {
    }
}
