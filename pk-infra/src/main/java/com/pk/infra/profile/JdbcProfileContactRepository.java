package com.pk.infra.profile;

import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.port.ProfileContactRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProfileContactRepository implements ProfileContactRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProfileContactRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProfileContactsModuleData> findModuleByProfileId(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT profile_id, mobile_no, module_status, last_request_id,
                           last_lender_request_json, last_lender_response_json
                    FROM user_profile_contacts
                    WHERE profile_id = ?
                    """,
                    (rs, rowNum) -> new ProfileContactsModuleData(
                            rs.getLong("profile_id"),
                            rs.getString("mobile_no"),
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
    public List<ProfileContactData> findContactsByProfileId(long profileId) {
        return jdbcTemplate.query(
                """
                SELECT mobile_no, sort_no, relationship, contact_name, contact_mobile
                FROM user_profile_contact
                WHERE profile_id = ?
                ORDER BY sort_no
                """,
                (rs, rowNum) -> new ProfileContactData(
                        rs.getString("mobile_no"),
                        rs.getInt("sort_no"),
                        rs.getInt("relationship"),
                        rs.getString("contact_name"),
                        rs.getString("contact_mobile")
                ),
                profileId
        );
    }

    @Override
    public void replaceContacts(
            long profileId,
            ProfileContactsModuleData module,
            List<ProfileContactData> contacts
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO user_profile_contacts (profile_id, mobile_no, module_status, last_request_id)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    mobile_no = VALUES(mobile_no),
                    module_status = VALUES(module_status),
                    last_request_id = VALUES(last_request_id)
                """,
                profileId,
                module.mobileNo(),
                module.moduleStatus(),
                module.lastRequestId()
        );

        jdbcTemplate.update("DELETE FROM user_profile_contact WHERE profile_id = ?", profileId);

        for (ProfileContactData contact : contacts) {
            jdbcTemplate.update(
                    """
                    INSERT INTO user_profile_contact (
                        profile_id,
                        mobile_no,
                        sort_no,
                        relationship,
                        contact_name,
                        contact_mobile
                    ) VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    profileId,
                    contact.mobileNo(),
                    contact.sortNo(),
                    contact.relationship(),
                    contact.contactName(),
                    contact.contactMobile()
            );
        }
    }

    @Override
    public void updateLastLenderAudit(long profileId, String requestDataJson, String responseDataJson) {
        jdbcTemplate.update(
                """
                UPDATE user_profile_contacts
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
