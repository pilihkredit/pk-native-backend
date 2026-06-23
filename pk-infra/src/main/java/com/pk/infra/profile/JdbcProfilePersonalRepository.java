package com.pk.infra.profile;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfilePersonalRepository;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProfilePersonalRepository implements ProfilePersonalRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProfilePersonalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProfilePersonalData> findByProfileId(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT profile_id, province_code, city_code, district_code, address, education_degree,
                           mother_surname_ciphertext, mother_surname_nonce, mother_surname_tag,
                           user_email, module_status, last_request_id
                    FROM user_profile_personal
                    WHERE profile_id = ?
                    """,
                    (rs, rowNum) -> new ProfilePersonalData(
                            rs.getLong("profile_id"),
                            rs.getString("province_code"),
                            rs.getString("city_code"),
                            rs.getString("district_code"),
                            rs.getString("address"),
                            rs.getInt("education_degree"),
                            new EncryptedField(
                                    rs.getString("mother_surname_ciphertext"),
                                    rs.getBytes("mother_surname_nonce"),
                                    rs.getBytes("mother_surname_tag")
                            ),
                            rs.getString("user_email"),
                            rs.getString("module_status"),
                            rs.getString("last_request_id")
                    ),
                    profileId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void upsert(ProfilePersonalData data) {
        jdbcTemplate.update(
                """
                INSERT INTO user_profile_personal (
                    profile_id,
                    province_code,
                    city_code,
                    district_code,
                    address,
                    education_degree,
                    mother_surname_ciphertext,
                    mother_surname_nonce,
                    mother_surname_tag,
                    user_email,
                    module_status,
                    last_request_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    province_code = VALUES(province_code),
                    city_code = VALUES(city_code),
                    district_code = VALUES(district_code),
                    address = VALUES(address),
                    education_degree = VALUES(education_degree),
                    mother_surname_ciphertext = VALUES(mother_surname_ciphertext),
                    mother_surname_nonce = VALUES(mother_surname_nonce),
                    mother_surname_tag = VALUES(mother_surname_tag),
                    user_email = VALUES(user_email),
                    module_status = VALUES(module_status),
                    last_request_id = VALUES(last_request_id)
                """,
                data.profileId(),
                data.provinceCode(),
                data.cityCode(),
                data.districtCode(),
                data.address(),
                data.educationDegree(),
                data.motherSurname().ciphertextBase64(),
                data.motherSurname().nonce(),
                data.motherSurname().tag(),
                data.userEmail(),
                data.moduleStatus(),
                data.lastRequestId()
        );
    }

    @Override
    public void updateEmail(long profileId, String userEmail) {
        jdbcTemplate.update(
                "UPDATE user_profile SET email = ? WHERE id = ? AND deleted_at IS NULL",
                userEmail,
                profileId
        );
    }
}
