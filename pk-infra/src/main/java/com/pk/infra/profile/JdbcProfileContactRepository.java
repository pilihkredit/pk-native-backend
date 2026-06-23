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
                    SELECT profile_id, module_status, last_request_id
                    FROM user_profile_contacts
                    WHERE profile_id = ?
                    """,
                    (rs, rowNum) -> new ProfileContactsModuleData(
                            rs.getLong("profile_id"),
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
    public void replaceContacts(
            long profileId,
            ProfileContactsModuleData module,
            List<ProfileContactData> contacts
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO user_profile_contacts (profile_id, module_status, last_request_id)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    module_status = VALUES(module_status),
                    last_request_id = VALUES(last_request_id)
                """,
                profileId,
                module.moduleStatus(),
                module.lastRequestId()
        );

        jdbcTemplate.update("DELETE FROM user_profile_contact WHERE profile_id = ?", profileId);

        for (ProfileContactData contact : contacts) {
            jdbcTemplate.update(
                    """
                    INSERT INTO user_profile_contact (
                        profile_id,
                        sort_no,
                        relationship,
                        contact_name,
                        contact_mobile
                    ) VALUES (?, ?, ?, ?, ?)
                    """,
                    profileId,
                    contact.sortNo(),
                    contact.relationship(),
                    contact.contactName(),
                    contact.contactMobile()
            );
        }
    }
}
