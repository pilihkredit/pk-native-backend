package com.pk.core.repay.port;

import java.time.Instant;
import java.util.List;

public interface RepayVaSnapshotRepository {
    void replaceSnapshots(
            long profileId,
            String snapshotNo,
            List<VaSnapshotInsert> snapshots,
            Long externalInteractionId,
            Instant fetchedAt
    );

    List<VaSnapshotRecord> findLatestByProfileId(long profileId);

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
            long profileId,
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
