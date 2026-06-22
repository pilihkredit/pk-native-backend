package com.pk.infra.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserProfileRepository {
    private static final RowMapper<UserProfileRecord> ROW_MAPPER = (rs, rowNum) -> new UserProfileRecord(
            rs.getLong("id"),
            rs.getString("partner_user_id"),
            rs.getString("mobile_no"),
            rs.getString("kyc_status"),
            rs.getTimestamp("created_at").toInstant()
    );

    private final JdbcTemplate jdbcTemplate;

    public UserProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserProfileRecord> findActiveByMobileNo(String mobileNo) {
        String sql = """
                SELECT id, partner_user_id, mobile_no, kyc_status, created_at
                FROM user_profile
                WHERE mobile_no = ? AND deleted_at IS NULL
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, mobileNo).stream().findFirst();
    }

    public Optional<UserProfileRecord> findActiveByPartnerUserId(String partnerUserId) {
        String sql = """
                SELECT id, partner_user_id, mobile_no, kyc_status, created_at
                FROM user_profile
                WHERE partner_user_id = ? AND deleted_at IS NULL
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, partnerUserId).stream().findFirst();
    }

    public UserProfileRecord insertNewUser(String mobileNo) {
        String sql = """
                INSERT INTO user_profile (
                    partner_user_id,
                    mobile_no,
                    kyc_status,
                    data_lifecycle_status,
                    retention_policy_code
                ) VALUES (?, ?, ?, ?, ?)
                """;
        String placeholderPartnerUserId = "TMP-" + UUID.randomUUID();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, placeholderPartnerUserId);
            ps.setString(2, mobileNo);
            ps.setString(3, "INCOMPLETE");
            ps.setString(4, "ACTIVE");
            ps.setString(5, "DEFAULT_PK_USER");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert user profile");
        }
        long id = key.longValue();
        String partnerUserId = formatPartnerUserId(id);
        jdbcTemplate.update(
                "UPDATE user_profile SET partner_user_id = ? WHERE id = ?",
                partnerUserId,
                id
        );
        return new UserProfileRecord(id, partnerUserId, mobileNo, "INCOMPLETE", java.time.Instant.now());
    }

    static String formatPartnerUserId(long id) {
        return "U" + String.format("%05d", id);
    }
}
