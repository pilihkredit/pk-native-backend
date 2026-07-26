package com.pk.core.repay.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RepayCurrentOrderRepository {
    CurrentOrderRecord upsertActive(CurrentOrderUpsert command);

    Optional<CurrentOrderRecord> findActiveByUserId(long userId);

    record CurrentOrderUpsert(
            long userId,
            String currentOrderNo,
            long trialId,
            String repayOrdersJson,
            Long couponId,
            Instant submittedAt
    ) {
    }

    record CurrentOrderRecord(
            long id,
            long userId,
            String currentOrderNo,
            long trialId,
            String repayOrdersJson,
            Long couponId,
            String status,
            Instant submittedAt
    ) {
    }
}
