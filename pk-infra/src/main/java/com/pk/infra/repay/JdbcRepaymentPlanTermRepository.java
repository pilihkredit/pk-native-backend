package com.pk.infra.repay;

import com.pk.core.repay.port.RepaymentPlanTermRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcRepaymentPlanTermRepository implements RepaymentPlanTermRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcRepaymentPlanTermRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void upsertTerms(
            long loanApplicationId,
            String loanApplyId,
            String billNo,
            List<TermUpsert> terms,
            Instant syncedAt
    ) {
        for (TermUpsert term : terms) {
            jdbcTemplate.update(
                    """
                    INSERT INTO repayment_plan_term (
                        loan_application_id, loan_apply_id, bill_no, sub_bill_no, term_no, term_status,
                        due_date, grace_date, schd_amount, should_amount, paid_amount, overdue_days,
                        amount_detail_json, last_repay_time, synced_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        loan_apply_id = VALUES(loan_apply_id),
                        bill_no = VALUES(bill_no),
                        sub_bill_no = VALUES(sub_bill_no),
                        term_status = VALUES(term_status),
                        due_date = VALUES(due_date),
                        grace_date = VALUES(grace_date),
                        schd_amount = VALUES(schd_amount),
                        should_amount = VALUES(should_amount),
                        paid_amount = VALUES(paid_amount),
                        overdue_days = VALUES(overdue_days),
                        amount_detail_json = VALUES(amount_detail_json),
                        last_repay_time = VALUES(last_repay_time),
                        synced_at = VALUES(synced_at)
                    """,
                    loanApplicationId,
                    loanApplyId,
                    billNo,
                    term.subBillNo(),
                    term.termNo(),
                    term.termStatus(),
                    term.dueDate() == null ? null : Timestamp.from(term.dueDate()),
                    term.graceDate() == null ? null : Timestamp.from(term.graceDate()),
                    term.schdAmount(),
                    term.shouldAmount(),
                    term.paidAmount(),
                    term.overdueDays(),
                    term.amountDetailJson(),
                    term.lastRepayTime() == null ? null : Timestamp.from(term.lastRepayTime()),
                    Timestamp.from(syncedAt)
            );
        }
    }

    @Override
    public List<TermRecord> findByLoanApplicationId(long loanApplicationId) {
        return jdbcTemplate.query(
                """
                SELECT id, loan_application_id, loan_apply_id, bill_no, sub_bill_no, term_no, term_status,
                       due_date, grace_date, schd_amount, should_amount, paid_amount, overdue_days,
                       amount_detail_json, last_repay_time, synced_at
                FROM repayment_plan_term
                WHERE loan_application_id = ?
                ORDER BY term_no ASC
                """,
                (rs, rowNum) -> mapRecord(rs),
                loanApplicationId
        );
    }

    @Override
    public List<TermRecord> findPendingByProfileId(long profileId) {
        return jdbcTemplate.query(
                """
                SELECT rpt.id, rpt.loan_application_id, rpt.loan_apply_id, rpt.bill_no, rpt.sub_bill_no,
                       rpt.term_no, rpt.term_status, rpt.due_date, rpt.grace_date, rpt.schd_amount,
                       rpt.should_amount, rpt.paid_amount, rpt.overdue_days, rpt.amount_detail_json,
                       rpt.last_repay_time, rpt.synced_at
                FROM repayment_plan_term rpt
                INNER JOIN loan_application la ON la.id = rpt.loan_application_id
                WHERE la.profile_id = ?
                  AND rpt.term_status IN ('UNPAID', 'OVERDUE')
                ORDER BY rpt.due_date ASC, rpt.term_no ASC
                """,
                (rs, rowNum) -> mapRecord(rs),
                profileId
        );
    }

    private static TermRecord mapRecord(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new TermRecord(
                rs.getLong("id"),
                rs.getLong("loan_application_id"),
                rs.getString("loan_apply_id"),
                rs.getString("bill_no"),
                rs.getString("sub_bill_no"),
                rs.getInt("term_no"),
                rs.getString("term_status"),
                toInstant(rs.getTimestamp("due_date")),
                toInstant(rs.getTimestamp("grace_date")),
                rs.getBigDecimal("schd_amount"),
                rs.getBigDecimal("should_amount"),
                rs.getBigDecimal("paid_amount"),
                rs.getInt("overdue_days"),
                rs.getString("amount_detail_json"),
                toInstant(rs.getTimestamp("last_repay_time")),
                toInstant(rs.getTimestamp("synced_at"))
        );
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
