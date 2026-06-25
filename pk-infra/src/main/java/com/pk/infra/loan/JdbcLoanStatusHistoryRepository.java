package com.pk.infra.loan;

import com.pk.core.loan.port.LoanStatusHistoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLoanStatusHistoryRepository implements LoanStatusHistoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLoanStatusHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(
            long loanApplicationId,
            String fromStatus,
            String toStatus,
            String externalStatus,
            String source
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO loan_status_history (
                    loan_application_id,
                    from_status,
                    to_status,
                    external_status,
                    source
                ) VALUES (?, ?, ?, ?, ?)
                """,
                loanApplicationId,
                fromStatus,
                toStatus,
                externalStatus,
                source
        );
    }
}
