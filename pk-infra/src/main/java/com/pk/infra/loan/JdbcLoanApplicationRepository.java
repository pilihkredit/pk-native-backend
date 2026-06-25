package com.pk.infra.loan;

import com.pk.core.loan.port.LoanApplicationRepository;
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
public class JdbcLoanApplicationRepository implements LoanApplicationRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLoanApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<LoanApplicationRecord> findById(long id) {
        return findOne("id = ?", id);
    }

    @Override
    public Optional<LoanApplicationRecord> findByLoanApplyId(String loanApplyId) {
        return findOne("loan_apply_id = ?", loanApplyId);
    }

    @Override
    public Optional<LoanApplicationRecord> findByLoanApplyIdAndProfileId(String loanApplyId, long profileId) {
        return findOne("loan_apply_id = ? AND profile_id = ?", loanApplyId, profileId);
    }

    @Override
    public long insert(LoanApplicationInsert insert) {
        jdbcTemplate.update(
                """
                INSERT INTO loan_application (
                    loan_apply_id,
                    credit_application_id,
                    quote_id,
                    profile_id,
                    profile_version_id,
                    status,
                    apply_amt,
                    loan_purpose,
                    submitted_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3))
                """,
                insert.loanApplyId(),
                insert.creditApplicationId(),
                insert.quoteId(),
                insert.profileId(),
                insert.profileVersionId(),
                insert.status(),
                insert.applyAmt(),
                insert.loanPurpose()
        );
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM loan_application WHERE loan_apply_id = ?",
                Long.class,
                insert.loanApplyId()
        );
        if (id == null) {
            throw new IllegalStateException("Failed to load inserted loan application");
        }
        return id;
    }

    @Override
    public void updateStatus(long id, String status, String externalStatus) {
        jdbcTemplate.update(
                """
                UPDATE loan_application
                SET status = ?,
                    external_status = ?,
                    finalized_at = CASE WHEN ? IN ('DISBURSED', 'REJECTED', 'FAILED', 'CANCEL')
                        THEN CURRENT_TIMESTAMP(3) ELSE finalized_at END
                WHERE id = ?
                """,
                status,
                externalStatus,
                status,
                id
        );
    }

    @Override
    public void markSubmitted(long id, String externalLoanApplyNo, String externalStatus) {
        jdbcTemplate.update(
                """
                UPDATE loan_application
                SET status = 'PROCESSING',
                    external_loan_apply_no = ?,
                    external_status = ?,
                    submitted_at = COALESCE(submitted_at, CURRENT_TIMESTAMP(3))
                WHERE id = ?
                """,
                externalLoanApplyNo,
                externalStatus,
                id
        );
    }

    @Override
    public void updateDisbursementDetails(
            long id,
            String billNo,
            java.math.BigDecimal applyAmt,
            java.math.BigDecimal payAmount,
            Instant payTime
    ) {
        jdbcTemplate.update(
                """
                UPDATE loan_application
                SET bill_no = ?,
                    apply_amt = COALESCE(?, apply_amt),
                    pay_amount = ?,
                    pay_time = ?
                WHERE id = ?
                """,
                billNo,
                applyAmt,
                payAmount,
                payTime == null ? null : Timestamp.from(payTime),
                id
        );
    }

    @Override
    public void scheduleNextPoll(long id, Instant nextPollAt) {
        jdbcTemplate.update(
                "UPDATE loan_application SET next_poll_at = ? WHERE id = ?",
                Timestamp.from(nextPollAt),
                id
        );
    }

    @Override
    public List<LoanApplicationRecord> findPendingPoll(int limit) {
        return jdbcTemplate.query(
                """
                SELECT id, loan_apply_id, credit_application_id, quote_id, profile_id, profile_version_id,
                       external_loan_apply_no, bill_no, status, external_status, apply_amt, pay_amount, pay_time
                FROM loan_application
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

    private Optional<LoanApplicationRecord> findOne(String whereClause, Object... args) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT id, loan_apply_id, credit_application_id, quote_id, profile_id, profile_version_id,
                           external_loan_apply_no, bill_no, status, external_status, apply_amt, pay_amount, pay_time
                    FROM loan_application
                    WHERE %s
                    """.formatted(whereClause),
                    (rs, rowNum) -> mapRecord(rs),
                    args
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private static LoanApplicationRecord mapRecord(ResultSet rs) throws SQLException {
        Timestamp payTime = rs.getTimestamp("pay_time");
        return new LoanApplicationRecord(
                rs.getLong("id"),
                rs.getString("loan_apply_id"),
                rs.getLong("credit_application_id"),
                rs.getLong("quote_id"),
                rs.getLong("profile_id"),
                rs.getLong("profile_version_id"),
                rs.getString("external_loan_apply_no"),
                rs.getString("bill_no"),
                rs.getString("status"),
                rs.getString("external_status"),
                rs.getBigDecimal("apply_amt"),
                rs.getBigDecimal("pay_amount"),
                payTime == null ? null : payTime.toInstant()
        );
    }
}
