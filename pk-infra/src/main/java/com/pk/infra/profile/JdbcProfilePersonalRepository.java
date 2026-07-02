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
                    SELECT profile_id, mobile_no, education_degree, industry, income,
                           mother_surname_ciphertext, mother_surname_nonce, mother_surname_tag,
                           user_email, module_status, last_request_id,
                           last_lender_request_json, last_lender_response_json
                    FROM user_profile_personal
                    WHERE profile_id = ?
                    """,
                    (rs, rowNum) -> new ProfilePersonalData(
                            rs.getLong("profile_id"),
                            rs.getString("mobile_no"),
                            rs.getInt("education_degree"),
                            rs.getInt("industry"),
                            rs.getString("income"),
                            new EncryptedField(
                                    rs.getString("mother_surname_ciphertext"),
                                    rs.getBytes("mother_surname_nonce"),
                                    rs.getBytes("mother_surname_tag")
                            ),
                            rs.getString("user_email"),
                            rs.getString("module_status"),
                            rs.getString("last_request_id"),
                            rs.getString("last_lender_request_json"),
                            rs.getString("last_lender_response_json")
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
                    mobile_no,
                    education_degree,
                    industry,
                    income,
                    mother_surname_ciphertext,
                    mother_surname_nonce,
                    mother_surname_tag,
                    user_email,
                    module_status,
                    last_request_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    mobile_no = VALUES(mobile_no),
                    education_degree = VALUES(education_degree),
                    industry = VALUES(industry),
                    income = VALUES(income),
                    mother_surname_ciphertext = VALUES(mother_surname_ciphertext),
                    mother_surname_nonce = VALUES(mother_surname_nonce),
                    mother_surname_tag = VALUES(mother_surname_tag),
                    user_email = VALUES(user_email),
                    module_status = VALUES(module_status),
                    last_request_id = VALUES(last_request_id)
                """,
                data.profileId(),
                data.mobileNo(),
                data.educationDegree(),
                data.industry(),
                data.income(),
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

    @Override
    public void updateLastLenderAudit(long profileId, String requestDataJson, String responseDataJson) {
        jdbcTemplate.update(
                """
                UPDATE user_profile_personal
                SET last_lender_request_json = ?,
                    last_lender_response_json = ?
                WHERE profile_id = ?
                """,
                requestDataJson,
                responseDataJson,
                profileId
        );
    }
}
