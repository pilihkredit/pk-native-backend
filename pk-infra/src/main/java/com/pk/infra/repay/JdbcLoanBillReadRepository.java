package com.pk.infra.repay;

import com.pk.core.repay.port.LoanBillReadRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLoanBillReadRepository implements LoanBillReadRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLoanBillReadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<LoanBillRecord> findByProfileIdAndBillFilter(long profileId, BillFilter filter) {
        String sql = """
                SELECT la.id, la.loan_apply_id, la.external_loan_apply_no, la.bill_no, la.apply_amt, la.status
                FROM loan_application la
                WHERE la.profile_id = ?
                """;
        if (filter == BillFilter.ACTIVE) {
            sql += """
                     AND la.status = 'DISBURSED'
                     AND EXISTS (
                         SELECT 1 FROM repayment_plan_term rpt
                         WHERE rpt.loan_application_id = la.id
                           AND rpt.term_status IN ('UNPAID', 'OVERDUE')
                     )
                    """;
        } else {
            sql += """
                     AND (
                         la.status = 'SETTLED'
                         OR NOT EXISTS (
                             SELECT 1 FROM repayment_plan_term rpt
                             WHERE rpt.loan_application_id = la.id
                               AND rpt.term_status IN ('UNPAID', 'OVERDUE')
                         )
                     )
                    """;
        }
        sql += " ORDER BY la.id DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRecord(rs), profileId);
    }

    @Override
    public Optional<LoanBillRecord> findByProfileIdAndLoanApplyId(long profileId, String loanApplyId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT la.id, la.loan_apply_id, la.external_loan_apply_no, la.bill_no, la.apply_amt, la.status
                    FROM loan_application la
                    WHERE la.profile_id = ? AND la.loan_apply_id = ?
                    """,
                    (rs, rowNum) -> mapRecord(rs),
                    profileId,
                    loanApplyId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<LoanBillRecord> findPendingByProfileId(long profileId) {
        return jdbcTemplate.query(
                """
                SELECT DISTINCT la.id, la.loan_apply_id, la.external_loan_apply_no, la.bill_no, la.apply_amt, la.status
                FROM loan_application la
                INNER JOIN repayment_plan_term rpt ON rpt.loan_application_id = la.id
                WHERE la.profile_id = ?
                  AND rpt.term_status IN ('UNPAID', 'OVERDUE')
                ORDER BY la.id DESC
                """,
                (rs, rowNum) -> mapRecord(rs),
                profileId
        );
    }

    private static LoanBillRecord mapRecord(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new LoanBillRecord(
                rs.getLong("id"),
                rs.getString("loan_apply_id"),
                rs.getString("external_loan_apply_no"),
                rs.getString("bill_no"),
                rs.getBigDecimal("apply_amt"),
                rs.getString("status")
        );
    }
}
