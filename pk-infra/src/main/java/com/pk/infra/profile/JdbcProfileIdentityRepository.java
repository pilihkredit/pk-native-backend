package com.pk.infra.profile;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.port.ProfileIdentityRepository;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProfileIdentityRepository implements ProfileIdentityRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProfileIdentityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProfileIdentityData> findByProfileId(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT profile_id, mobile_no, full_name,
                           id_no_ciphertext, id_no_nonce, id_no_tag,
                           id_no_hash, module_status, last_request_id,
                           last_lender_request_json, last_lender_response_json
                    FROM user_profile_identity
                    WHERE profile_id = ?
                    """,
                    (rs, rowNum) -> new ProfileIdentityData(
                            rs.getLong("profile_id"),
                            rs.getString("mobile_no"),
                            rs.getString("full_name"),
                            new EncryptedField(
                                    rs.getString("id_no_ciphertext"),
                                    rs.getBytes("id_no_nonce"),
                                    rs.getBytes("id_no_tag")
                            ),
                            rs.getString("id_no_hash"),
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
    public void upsert(ProfileIdentityData data) {
        jdbcTemplate.update(
                """
                INSERT INTO user_profile_identity (
                    profile_id,
                    mobile_no,
                    full_name,
                    id_no_ciphertext,
                    id_no_nonce,
                    id_no_tag,
                    id_no_hash,
                    module_status,
                    last_request_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    mobile_no = VALUES(mobile_no),
                    full_name = VALUES(full_name),
                    id_no_ciphertext = VALUES(id_no_ciphertext),
                    id_no_nonce = VALUES(id_no_nonce),
                    id_no_tag = VALUES(id_no_tag),
                    id_no_hash = VALUES(id_no_hash),
                    module_status = VALUES(module_status),
                    last_request_id = VALUES(last_request_id)
                """,
                data.profileId(),
                data.mobileNo(),
                data.fullName(),
                data.idNo().ciphertextBase64(),
                data.idNo().nonce(),
                data.idNo().tag(),
                data.idNoHash(),
                data.moduleStatus(),
                data.lastRequestId()
        );
    }

    @Override
    public void updateLastLenderAudit(long profileId, String requestJson, String responseJson) {
        jdbcTemplate.update(
                """
                UPDATE user_profile_identity
                SET last_lender_request_json = ?,
                    last_lender_response_json = ?
                WHERE profile_id = ?
                """,
                requestJson,
                responseJson,
                profileId
        );
    }
}
