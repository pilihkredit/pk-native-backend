package com.pk.core.repay.port;

import java.time.Instant;
import java.util.List;

public interface RepayVaSnapshotRepository {
    void replaceSnapshots(
            long userId,
            String snapshotNo,
            List<VaSnapshotInsert> snapshots,
            Long externalInteractionId,
            Instant fetchedAt
    );

    List<VaSnapshotRecord> findLatestByUserId(long userId);

    record VaSnapshotInsert(
            String vaNo,
            String bankCode,
            String bankName,
            boolean defaultFlag,
            boolean disabled,
            String bankChannelsJson
    ) {
    }

    record VaSnapshotRecord(
            long id,
            long userId,
            String snapshotNo,
            String vaNo,
            String bankCode,
            String bankName,
            boolean defaultFlag,
            boolean disabled,
            String bankChannelsJson,
            Instant fetchedAt
    ) {
    }
}
