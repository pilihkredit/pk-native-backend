package com.pk.infra.auth;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserAuthRepository implements UserAuthRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserAuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserProfileSummary> findByMobileNo(String mobileNo) {
        return queryProfile("mobile_no = ?", mobileNo);
    }

    @Override
    public Optional<UserProfileSummary> findByProfileId(long profileId) {
        return queryProfile("id = ?", profileId);
    }

    private Optional<UserProfileSummary> queryProfile(String predicate, Object argument) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    "SELECT id, partner_user_id, mobile_no FROM user_profile WHERE " + predicate + " AND deleted_at IS NULL LIMIT 1",
                    (rs, rowNum) -> new UserProfileSummary(
                            rs.getLong("id"),
                            rs.getString("partner_user_id"),
                            rs.getString("mobile_no"),
                            false
                    ),
                    argument
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public UserProfileSummary createByMobileNo(String mobileNo) {
        String partnerUserId = "U" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        jdbcTemplate.update(
                """
                INSERT INTO user_profile (
                    partner_user_id,
                    mobile_no,
                    kyc_status,
                    data_lifecycle_status,
                    data_residency_country,
                    retention_policy_code
                ) VALUES (?, ?, 'PENDING', 'ACTIVE', 'ID', 'DEFAULT')
                """,
                partnerUserId,
                mobileNo
        );
        return findByMobileNo(mobileNo)
                .map(profile -> new UserProfileSummary(profile.profileId(), profile.partnerUserId(), profile.mobileNo(), true))
                .orElseThrow(() -> new IllegalStateException("Failed to load created user profile"));
    }
}
