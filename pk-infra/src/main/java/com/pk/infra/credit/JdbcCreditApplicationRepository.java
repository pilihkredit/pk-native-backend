package com.pk.infra.credit;

import com.pk.core.credit.port.CreditApplicationRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCreditApplicationRepository implements CreditApplicationRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCreditApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CreditApplicationRecord> findById(long id) {
        return findOne("id = ?", id);
    }

    @Override
    public Optional<CreditApplicationRecord> findByRequestId(String requestId) {
        return findOne("request_id = ?", requestId);
    }

    @Override
    public Optional<CreditApplicationRecord> findByApplyIdAndProfileId(String applyId, long profileId) {
        return findOne("apply_id = ? AND profile_id = ?", applyId, profileId);
    }

    @Override
    public Optional<CreditApplicationRecord> findByApplyId(String applyId) {
        return findOne("apply_id = ?", applyId);
    }

    @Override
    public long insert(CreditApplicationInsert insert) {
        jdbcTemplate.update(
                """
                INSERT INTO credit_application (
                    apply_id,
                    request_id,
                    provider_code,
                    profile_id,
                    profile_version_id,
                    status,
                    submitted_at
                ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3))
                """,
                insert.applyId(),
                insert.requestId(),
                insert.providerCode(),
                insert.profileId(),
                insert.profileVersionId(),
                insert.status()
        );
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM credit_application WHERE apply_id = ?",
                Long.class,
                insert.applyId()
        );
        if (id == null) {
            throw new IllegalStateException("Failed to load inserted credit application");
        }
        return id;
    }

    @Override
    public void updateStatus(long id, String status, String externalStatus, String lastErrorCode) {
        jdbcTemplate.update(
                """
                UPDATE credit_application
                SET status = ?,
                    external_status = ?,
                    last_error_code = ?,
                    finalized_at = CASE WHEN ? IN ('APPROVED', 'REJECTED', 'FAILED', 'CANCEL')
                        THEN CURRENT_TIMESTAMP(3) ELSE finalized_at END
                WHERE id = ?
                """,
                status,
                externalStatus,
                lastErrorCode,
                status,
                id
        );
    }

    @Override
    public void markSubmitted(long id, String externalCreditApplyNo, String externalStatus) {
        jdbcTemplate.update(
                """
                UPDATE credit_application
                SET status = 'PROCESSING',
                    external_credit_apply_no = ?,
                    external_status = ?,
                    submitted_at = COALESCE(submitted_at, CURRENT_TIMESTAMP(3))
                WHERE id = ?
                """,
                externalCreditApplyNo,
                externalStatus,
                id
        );
    }

    @Override
    public void scheduleNextPoll(long id, Instant nextPollAt) {
        jdbcTemplate.update(
                "UPDATE credit_application SET next_poll_at = ? WHERE id = ?",
                Timestamp.from(nextPollAt),
                id
        );
    }

    @Override
    public void updateFreezeEndAt(long id, Instant freezeEndAt) {
        jdbcTemplate.update(
                "UPDATE credit_application SET freeze_end_at = ? WHERE id = ?",
                freezeEndAt == null ? null : Timestamp.from(freezeEndAt),
                id
        );
    }

    @Override
    public List<CreditApplicationRecord> findDueForPoll(int limit) {
        return jdbcTemplate.query(
                """
                SELECT id, apply_id, request_id, provider_code, profile_id, profile_version_id,
                       external_credit_apply_no, status, external_status, freeze_end_at
                FROM credit_application
                WHERE status = 'PROCESSING'
                  AND next_poll_at IS NOT NULL
                  AND next_poll_at <= CURRENT_TIMESTAMP(3)
                ORDER BY next_poll_at
                LIMIT ?
                """,
                (rs, rowNum) -> mapRecord(rs),
                limit
        );
    }

    private Optional<CreditApplicationRecord> findOne(String whereClause, Object... args) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT id, apply_id, request_id, provider_code, profile_id, profile_version_id,
                           external_credit_apply_no, status, external_status, freeze_end_at
                    FROM credit_application
                    WHERE %s
                    """.formatted(whereClause),
                    (rs, rowNum) -> mapRecord(rs),
                    args
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private static CreditApplicationRecord mapRecord(ResultSet rs) throws SQLException {
        Timestamp freezeEndAt = rs.getTimestamp("freeze_end_at");
        return new CreditApplicationRecord(
                rs.getLong("id"),
                rs.getString("apply_id"),
                rs.getString("request_id"),
                rs.getString("provider_code"),
                rs.getLong("profile_id"),
                rs.getLong("profile_version_id"),
                rs.getString("external_credit_apply_no"),
                rs.getString("status"),
                rs.getString("external_status"),
                freezeEndAt == null ? null : freezeEndAt.toInstant()
        );
    }
}
