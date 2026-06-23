package com.pk.infra.auth;

import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.port.SmsSendLogRepository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSmsSendLogRepository implements SmsSendLogRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcSmsSendLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countSince(String mobileNo, Instant sinceInclusive) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sms_send_log WHERE mobile_no = ? AND created_at >= ?",
                Long.class,
                mobileNo,
                Timestamp.from(sinceInclusive)
        );
        return count == null ? 0L : count;
    }

    @Override
    public long insert(SmsSendLogEntry entry) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO sms_send_log (
                        profile_id,
                        mobile_no,
                        device_no,
                        otp_code,
                        purpose,
                        provider_success
                    ) VALUES (?, ?, ?, ?, ?, 0)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            if (entry.profileId().isPresent()) {
                statement.setLong(1, entry.profileId().get());
            } else {
                statement.setObject(1, null);
            }
            statement.setString(2, entry.mobileNo());
            statement.setString(3, entry.deviceNo());
            statement.setString(4, entry.otpCode());
            statement.setString(5, entry.purpose());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to persist sms_send_log row");
        }
        return key.longValue();
    }

    @Override
    public void updateProviderResult(long logId, SmsSendResult result) {
        jdbcTemplate.update(
                """
                UPDATE sms_send_log
                SET provider_code = ?,
                    provider_message_id = ?,
                    provider_success = ?,
                    provider_error_code = ?,
                    provider_error_message = ?
                WHERE id = ?
                """,
                result.providerCode(),
                result.providerMessageId(),
                result.success() ? 1 : 0,
                result.errorCode(),
                result.errorMessage(),
                logId
        );
    }
}
