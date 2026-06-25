package com.pk.infra.repay;

import com.pk.core.repay.port.RepayVaSnapshotRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcRepayVaSnapshotRepository implements RepayVaSnapshotRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcRepayVaSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void replaceSnapshots(
            long profileId,
            String snapshotNo,
            List<VaSnapshotInsert> snapshots,
            Instant fetchedAt
    ) {
        for (VaSnapshotInsert snapshot : snapshots) {
            jdbcTemplate.update(
                    """
                    INSERT INTO repay_va_snapshot (
                        profile_id, snapshot_no, va_no, bank_code, bank_name,
                        default_flag, disabled, bank_channels_json, fetched_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    profileId,
                    snapshotNo,
                    snapshot.vaNo(),
                    snapshot.bankCode(),
                    snapshot.bankName(),
                    snapshot.defaultFlag() ? 1 : 0,
                    snapshot.disabled() ? 1 : 0,
                    snapshot.bankChannelsJson(),
                    Timestamp.from(fetchedAt)
            );
        }
    }

    @Override
    public List<VaSnapshotRecord> findLatestByProfileId(long profileId) {
        return jdbcTemplate.query(
                """
                SELECT rvs.id, rvs.profile_id, rvs.snapshot_no, rvs.va_no, rvs.bank_code, rvs.bank_name,
                       rvs.default_flag, rvs.disabled, rvs.bank_channels_json, rvs.fetched_at
                FROM repay_va_snapshot rvs
                INNER JOIN (
                    SELECT snapshot_no
                    FROM repay_va_snapshot
                    WHERE profile_id = ?
                    ORDER BY fetched_at DESC
                    LIMIT 1
                ) latest ON latest.snapshot_no = rvs.snapshot_no
                WHERE rvs.profile_id = ?
                ORDER BY rvs.default_flag DESC, rvs.id ASC
                """,
                (rs, rowNum) -> new VaSnapshotRecord(
                        rs.getLong("id"),
                        rs.getLong("profile_id"),
                        rs.getString("snapshot_no"),
                        rs.getString("va_no"),
                        rs.getString("bank_code"),
                        rs.getString("bank_name"),
                        rs.getInt("default_flag") == 1,
                        rs.getInt("disabled") == 1,
                        rs.getString("bank_channels_json"),
                        rs.getTimestamp("fetched_at").toInstant()
                ),
                profileId,
                profileId
        );
    }
}
