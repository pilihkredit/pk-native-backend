package com.pk.infra.credit;

import com.pk.core.credit.port.CreditStatusHistoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCreditStatusHistoryRepository implements CreditStatusHistoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCreditStatusHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(
            long creditApplicationId,
            String fromStatus,
            String toStatus,
            String externalStatus,
            String source
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO credit_status_history (
                    credit_application_id,
                    from_status,
                    to_status,
                    external_status,
                    source
                ) VALUES (?, ?, ?, ?, ?)
                """,
                creditApplicationId,
                fromStatus,
                toStatus,
                externalStatus,
                source
        );
    }
}
