package com.pk.infra.external;

import com.pk.core.external.LenderInteractionLog;
import com.pk.core.external.port.LenderInteractionLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLenderInteractionLogRepository implements LenderInteractionLogRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLenderInteractionLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(LenderInteractionLog log) {
        jdbcTemplate.update(
                """
                INSERT INTO external_interaction (
                    provider_code,
                    interaction_no,
                    business_type,
                    business_id,
                    http_method,
                    endpoint,
                    request_id,
                    request_hash,
                    request_ref,
                    response_code,
                    response_msg,
                    response_ref,
                    success,
                    duration_ms
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                log.providerCode(),
                log.interactionNo(),
                log.businessType(),
                log.businessId(),
                log.httpMethod(),
                log.endpoint(),
                log.requestId(),
                log.requestHash(),
                log.requestRef(),
                log.responseCode(),
                log.responseMsg(),
                log.responseRef(),
                log.success() ? 1 : 0,
                log.durationMs()
        );
    }
}
