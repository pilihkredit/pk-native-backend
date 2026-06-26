package com.pk.infra.credit;

import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCreditLimitSnapshotRepository implements CreditLimitSnapshotRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCreditLimitSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void upsert(CreditLimitSnapshotData data) {
        jdbcTemplate.update(
                "DELETE FROM credit_limit_snapshot WHERE credit_application_id = ?",
                data.creditApplicationId()
        );
        jdbcTemplate.update(
                """
                INSERT INTO credit_limit_snapshot (
                    credit_application_id,
                    risk_min_limit,
                    risk_max_limit,
                    psychological_credit_limit,
                    fake_credit_limit,
                    borrow_amt_step_size,
                    contract_expire_at,
                    source
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                data.creditApplicationId(),
                data.riskMinLimit(),
                data.riskMaxLimit(),
                data.psychologicalCreditLimit(),
                data.fakeCreditLimit(),
                data.borrowAmtStepSize(),
                data.contractExpireAt() == null ? null : Timestamp.from(data.contractExpireAt()),
                data.source()
        );
    }

    @Override
    public Optional<CreditLimitSnapshotData> findByCreditApplicationId(long creditApplicationId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT credit_application_id, risk_min_limit, risk_max_limit, psychological_credit_limit,
                           fake_credit_limit, borrow_amt_step_size, contract_expire_at, source
                    FROM credit_limit_snapshot
                    WHERE credit_application_id = ?
                    """,
                    (rs, rowNum) -> map(rs),
                    creditApplicationId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private static CreditLimitSnapshotData map(ResultSet rs) throws SQLException {
        Timestamp contractExpireAt = rs.getTimestamp("contract_expire_at");
        return new CreditLimitSnapshotData(
                rs.getLong("credit_application_id"),
                rs.getBigDecimal("risk_min_limit"),
                rs.getBigDecimal("risk_max_limit"),
                rs.getBigDecimal("psychological_credit_limit"),
                rs.getBigDecimal("fake_credit_limit"),
                rs.getBigDecimal("borrow_amt_step_size"),
                contractExpireAt == null ? null : contractExpireAt.toInstant(),
                rs.getString("source")
        );
    }
}
