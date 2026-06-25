package com.pk.core.loan.port;

import java.time.Instant;
import java.util.Optional;

public interface ProductSnapshotRepository {
    ProductSnapshotRecord insert(ProductSnapshotInsert command);

    Optional<ProductSnapshotRecord> findBySnapshotNo(String snapshotNo);

    Optional<ProductSnapshotRecord> findLatestByCreditApplicationId(long creditApplicationId);

    record ProductSnapshotInsert(
            String snapshotNo,
            String applyId,
            long creditApplicationId,
            String creditStatus,
            String productStatus,
            String productsJson,
            Instant fetchedAt
    ) {
    }

    record ProductSnapshotRecord(
            long id,
            String snapshotNo,
            String applyId,
            long creditApplicationId,
            String creditStatus,
            String productStatus,
            String productsJson,
            Instant fetchedAt
    ) {
    }
}
