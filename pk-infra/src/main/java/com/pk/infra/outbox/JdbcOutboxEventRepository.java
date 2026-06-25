package com.pk.infra.outbox;

import com.pk.core.outbox.OutboxEvent;
import com.pk.core.outbox.OutboxEventStatus;
import com.pk.core.outbox.port.OutboxEventRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcOutboxEventRepository implements OutboxEventRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcOutboxEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertPending(OutboxEventDraft draft) {
        jdbcTemplate.update(
                """
                INSERT INTO outbox_event (
                    event_no,
                    event_type,
                    aggregate_type,
                    aggregate_id,
                    payload_json,
                    status,
                    retry_count,
                    next_retry_at
                ) VALUES (?, ?, ?, ?, ?, ?, 0, NULL)
                """,
                draft.eventNo(),
                draft.eventType(),
                draft.aggregateType(),
                draft.aggregateId(),
                draft.payloadJson(),
                OutboxEventStatus.PENDING
        );
    }

    @Override
    public List<OutboxEvent> findReady(int limit) {
        return jdbcTemplate.query(
                """
                SELECT id, event_no, event_type, aggregate_type, aggregate_id, payload_json,
                       status, retry_count, next_retry_at
                FROM outbox_event
                WHERE status = ?
                  AND (next_retry_at IS NULL OR next_retry_at <= CURRENT_TIMESTAMP(3))
                ORDER BY id
                LIMIT ?
                """,
                (rs, rowNum) -> mapEvent(rs),
                OutboxEventStatus.PENDING,
                limit
        );
    }

    @Override
    public void markCompleted(long id) {
        jdbcTemplate.update(
                "UPDATE outbox_event SET status = ? WHERE id = ?",
                OutboxEventStatus.COMPLETED,
                id
        );
    }

    @Override
    public void markFailed(long id, int retryCount, Instant nextRetryAt) {
        jdbcTemplate.update(
                """
                UPDATE outbox_event
                SET status = ?, retry_count = ?, next_retry_at = ?
                WHERE id = ?
                """,
                OutboxEventStatus.PENDING,
                retryCount,
                Timestamp.from(nextRetryAt),
                id
        );
    }

    @Override
    public void markTerminalFailure(long id, int retryCount) {
        jdbcTemplate.update(
                "UPDATE outbox_event SET status = ?, retry_count = ? WHERE id = ?",
                OutboxEventStatus.FAILED,
                retryCount,
                id
        );
    }

    private static OutboxEvent mapEvent(ResultSet rs) throws SQLException {
        Timestamp nextRetryAt = rs.getTimestamp("next_retry_at");
        return new OutboxEvent(
                rs.getLong("id"),
                rs.getString("event_no"),
                rs.getString("event_type"),
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                rs.getString("payload_json"),
                rs.getString("status"),
                rs.getInt("retry_count"),
                nextRetryAt == null ? null : nextRetryAt.toInstant()
        );
    }
}
