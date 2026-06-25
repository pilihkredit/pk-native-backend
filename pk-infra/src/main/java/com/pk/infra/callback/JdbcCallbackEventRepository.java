package com.pk.infra.callback;

import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.port.CallbackEventRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCallbackEventRepository implements CallbackEventRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCallbackEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long insert(CallbackEventInsert insert) {
        jdbcTemplate.update(
                """
                INSERT INTO callback_event (
                    callback_no,
                    provider_code,
                    callback_type,
                    business_id,
                    idempotency_key,
                    external_status,
                    payload_json,
                    process_status,
                    received_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                insert.callbackNo(),
                insert.providerCode(),
                insert.callbackType(),
                insert.businessId(),
                insert.idempotencyKey(),
                insert.externalStatus(),
                insert.payloadJson(),
                insert.processStatus(),
                Timestamp.from(insert.receivedAt())
        );
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM callback_event WHERE callback_no = ?",
                Long.class,
                insert.callbackNo()
        );
        if (id == null) {
            throw new IllegalStateException("Failed to load inserted callback event");
        }
        return id;
    }

    @Override
    public Optional<CallbackEventRecord> findById(long id) {
        return findOne("id = ?", id);
    }

    @Override
    public Optional<CallbackEventRecord> findByIdempotencyKey(String idempotencyKey) {
        return findOne("idempotency_key = ?", idempotencyKey);
    }

    @Override
    public void markProcessed(long id, Instant processedAt) {
        jdbcTemplate.update(
                """
                UPDATE callback_event
                SET process_status = ?, processed_at = ?
                WHERE id = ?
                """,
                CallbackProcessStatus.PROCESSED,
                Timestamp.from(processedAt),
                id
        );
    }

    @Override
    public void markIgnored(long id, Instant processedAt) {
        jdbcTemplate.update(
                """
                UPDATE callback_event
                SET process_status = ?, processed_at = ?
                WHERE id = ?
                """,
                CallbackProcessStatus.IGNORED,
                Timestamp.from(processedAt),
                id
        );
    }

    private Optional<CallbackEventRecord> findOne(String whereClause, Object... args) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT id, callback_no, provider_code, callback_type, business_id,
                           idempotency_key, external_status, payload_json, process_status
                    FROM callback_event
                    WHERE %s
                    """.formatted(whereClause),
                    (rs, rowNum) -> mapRecord(rs),
                    args
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private static CallbackEventRecord mapRecord(ResultSet rs) throws SQLException {
        return new CallbackEventRecord(
                rs.getLong("id"),
                rs.getString("callback_no"),
                rs.getString("provider_code"),
                rs.getString("callback_type"),
                rs.getString("business_id"),
                rs.getString("idempotency_key"),
                rs.getString("external_status"),
                rs.getString("payload_json"),
                rs.getString("process_status")
        );
    }
}
