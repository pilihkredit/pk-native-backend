package com.pk.infra.profile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.port.ProfileDeviceRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProfileDeviceRepository implements ProfileDeviceRepository {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcProfileDeviceRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ProfileDeviceData> findByProfileId(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT profile_id, device_no, system_platform, client_app_name, app_version, package_name,
                           ad_id, device_other_json, last_request_id
                    FROM user_profile_device
                    WHERE profile_id = ?
                    """,
                    (rs, rowNum) -> new ProfileDeviceData(
                            rs.getLong("profile_id"),
                            rs.getString("device_no"),
                            rs.getString("system_platform"),
                            rs.getString("client_app_name"),
                            rs.getString("app_version"),
                            rs.getString("package_name"),
                            rs.getString("ad_id"),
                            readJsonMap(rs.getString("device_other_json")),
                            rs.getString("last_request_id")
                    ),
                    profileId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void upsert(ProfileDeviceData data) {
        jdbcTemplate.update(
                """
                INSERT INTO user_profile_device (
                    profile_id,
                    device_no,
                    system_platform,
                    client_app_name,
                    app_version,
                    package_name,
                    ad_id,
                    device_other_json,
                    last_request_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    device_no = VALUES(device_no),
                    system_platform = VALUES(system_platform),
                    client_app_name = VALUES(client_app_name),
                    app_version = VALUES(app_version),
                    package_name = VALUES(package_name),
                    ad_id = VALUES(ad_id),
                    device_other_json = VALUES(device_other_json),
                    last_request_id = VALUES(last_request_id)
                """,
                data.profileId(),
                data.deviceNo(),
                data.systemPlatform(),
                data.clientAppName(),
                data.appVersion(),
                data.packageName(),
                data.adId(),
                writeJson(data.deviceOtherInfo()),
                data.lastRequestId()
        );
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize device_other_json", exception);
        }
    }

    private String writeJson(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize device_other_json", exception);
        }
    }
}
