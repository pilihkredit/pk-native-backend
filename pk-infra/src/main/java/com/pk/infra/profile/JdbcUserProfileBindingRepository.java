package com.pk.infra.profile;

import com.pk.core.profile.port.UserProfileBindingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserProfileBindingRepository implements UserProfileBindingRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserProfileBindingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void recordLenderProfileSync(long profileId, String externalUserId) {
        if (externalUserId != null && !externalUserId.isBlank()) {
            jdbcTemplate.update(
                    """
                    UPDATE user_profile
                    SET external_user_id = ?,
                        last_synced_at = CURRENT_TIMESTAMP(3)
                    WHERE id = ? AND deleted_at IS NULL
                    """,
                    externalUserId.trim(),
                    profileId
            );
            return;
        }
        jdbcTemplate.update(
                """
                UPDATE user_profile
                SET last_synced_at = CURRENT_TIMESTAMP(3)
                WHERE id = ? AND deleted_at IS NULL
                """,
                profileId
        );
    }

    @Override
    public void updateKycStatus(long profileId, String kycStatus) {
        jdbcTemplate.update(
                """
                UPDATE user_profile
                SET kyc_status = ?,
                    updated_at = CURRENT_TIMESTAMP(3)
                WHERE id = ? AND deleted_at IS NULL
                """,
                kycStatus,
                profileId
        );
    }
}
