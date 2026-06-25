package com.pk.infra.repay;

import com.pk.core.repay.port.RepayCurrentOrderRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcRepayCurrentOrderRepository implements RepayCurrentOrderRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcRepayCurrentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public CurrentOrderRecord upsertActive(CurrentOrderUpsert command) {
        jdbcTemplate.update(
                """
                UPDATE repay_current_order
                SET status = 'INACTIVE'
                WHERE profile_id = ? AND status = 'ACTIVE'
                """,
                command.profileId()
        );
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    """
                    INSERT INTO repay_current_order (
                        profile_id, current_order_no, trial_id, repay_orders_json,
                        coupon_id, status, submitted_at
                    ) VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?)
                    """,
                    new String[] {"id"}
            );
            statement.setLong(1, command.profileId());
            statement.setString(2, command.currentOrderNo());
            statement.setLong(3, command.trialId());
            statement.setString(4, command.repayOrdersJson());
            if (command.couponId() == null) {
                statement.setObject(5, null);
            } else {
                statement.setLong(5, command.couponId());
            }
            statement.setTimestamp(6, Timestamp.from(command.submittedAt()));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert repay current order");
        }
        return new CurrentOrderRecord(
                key.longValue(),
                command.profileId(),
                command.currentOrderNo(),
                command.trialId(),
                command.repayOrdersJson(),
                command.couponId(),
                "ACTIVE",
                command.submittedAt()
        );
    }

    @Override
    public Optional<CurrentOrderRecord> findActiveByProfileId(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT id, profile_id, current_order_no, trial_id, repay_orders_json,
                           coupon_id, status, submitted_at
                    FROM repay_current_order
                    WHERE profile_id = ? AND status = 'ACTIVE'
                    ORDER BY id DESC
                    LIMIT 1
                    """,
                    (rs, rowNum) -> {
                        long couponId = rs.getLong("coupon_id");
                        Long couponIdValue = rs.wasNull() ? null : couponId;
                        return new CurrentOrderRecord(
                                rs.getLong("id"),
                                rs.getLong("profile_id"),
                                rs.getString("current_order_no"),
                                rs.getLong("trial_id"),
                                rs.getString("repay_orders_json"),
                                couponIdValue,
                                rs.getString("status"),
                                rs.getTimestamp("submitted_at").toInstant()
                        );
                    },
                    profileId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
