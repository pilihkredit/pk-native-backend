package com.pk.infra.home;

import com.pk.core.home.port.HomeLifecycleReadRepository;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcHomeLifecycleReadRepository implements HomeLifecycleReadRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcHomeLifecycleReadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CreditApplySnapshot> findLatestCreditApply(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT apply_id, status
                    FROM credit_application
                    WHERE profile_id = ?
                    ORDER BY id DESC
                    LIMIT 1
                    """,
                    (rs, rowNum) -> new CreditApplySnapshot(
                            rs.getString("apply_id"),
                            rs.getString("status")
                    ),
                    profileId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<LoanApplySnapshot> findLatestLoanApply(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT loan_apply_id, status
                    FROM loan_application
                    WHERE profile_id = ?
                    ORDER BY id DESC
                    LIMIT 1
                    """,
                    (rs, rowNum) -> new LoanApplySnapshot(
                            rs.getString("loan_apply_id"),
                            rs.getString("status")
                    ),
                    profileId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public int countPendingRepayLoans(long profileId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT rpt.loan_application_id)
                FROM repayment_plan_term rpt
                INNER JOIN loan_application la ON la.id = rpt.loan_application_id
                WHERE la.profile_id = ?
                  AND rpt.term_status IN ('UNPAID', 'OVERDUE')
                """,
                Integer.class,
                profileId
        );
        return count == null ? 0 : count;
    }

    @Override
    public boolean hasOverdueRepay(long profileId) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS(
                    SELECT 1
                    FROM repayment_plan_term rpt
                    INNER JOIN loan_application la ON la.id = rpt.loan_application_id
                    WHERE la.profile_id = ?
                      AND rpt.term_status IN ('UNPAID', 'OVERDUE')
                      AND (rpt.term_status = 'OVERDUE' OR COALESCE(rpt.overdue_days, 0) > 0)
                )
                """,
                Boolean.class,
                profileId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean hasDisbursedLoan(long profileId) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS(
                    SELECT 1
                    FROM loan_application
                    WHERE profile_id = ?
                      AND status = 'DISBURSED'
                )
                """,
                Boolean.class,
                profileId
        );
        return Boolean.TRUE.equals(exists);
    }
}
