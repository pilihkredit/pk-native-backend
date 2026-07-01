package com.pk.infra.profile;

import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.port.ProfileDeviceRepository;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProfileDeviceRepository implements ProfileDeviceRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProfileDeviceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByProfileId(long profileId) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS(
                    SELECT 1
                    FROM user_device
                    WHERE profile_id = ?
                    LIMIT 1
                )
                """,
                Boolean.class,
                profileId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Optional<ProfileDeviceData> findByDeviceNo(String deviceNo) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT profile_id, partner_user_id, device_no, system_platform, client_app_name, app_version,
                           package_name, ad_id, device_json, last_request_id
                    FROM user_device
                    WHERE device_no = ?
                    """,
                    (rs, rowNum) -> mapRow(rs),
                    deviceNo.trim()
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void upsertByDeviceNo(ProfileDeviceData data) {
        jdbcTemplate.update(
                """
                INSERT INTO user_device (
                    profile_id,
                    partner_user_id,
                    device_no,
                    system_platform,
                    client_app_name,
                    app_version,
                    package_name,
                    ad_id,
                    device_json,
                    last_request_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    profile_id = VALUES(profile_id),
                    partner_user_id = VALUES(partner_user_id),
                    system_platform = VALUES(system_platform),
                    client_app_name = VALUES(client_app_name),
                    app_version = VALUES(app_version),
                    package_name = VALUES(package_name),
                    ad_id = VALUES(ad_id),
                    device_json = VALUES(device_json),
                    last_request_id = VALUES(last_request_id),
                    updated_at = CURRENT_TIMESTAMP(3)
                """,
                data.profileId(),
                data.partnerUserId(),
                data.deviceNo(),
                data.systemPlatform(),
                data.clientAppName(),
                data.appVersion(),
                data.packageName(),
                data.adId(),
                data.deviceJson(),
                data.lastRequestId()
        );
    }

    private static ProfileDeviceData mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ProfileDeviceData(
                rs.getLong("profile_id"),
                rs.getString("partner_user_id"),
                rs.getString("device_no"),
                rs.getString("system_platform"),
                rs.getString("client_app_name"),
                rs.getString("app_version"),
                rs.getString("package_name"),
                rs.getString("ad_id"),
                rs.getString("device_json"),
                rs.getString("last_request_id")
        );
    }
}
