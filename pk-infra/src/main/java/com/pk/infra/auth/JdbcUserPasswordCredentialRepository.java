package com.pk.infra.auth;

import com.pk.core.auth.port.UserPasswordCredentialRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserPasswordCredentialRepository implements UserPasswordCredentialRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserPasswordCredentialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isPasswordSet(long profileId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_password_credential WHERE profile_id = ?",
                Integer.class,
                profileId
        );
        return count != null && count > 0;
    }

    @Override
    public Optional<PasswordCredential> findByProfileId(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT profile_id, password_hash, failed_attempts, locked_until
                    FROM user_password_credential
                    WHERE profile_id = ?
                    LIMIT 1
                    """,
                    (rs, rowNum) -> new PasswordCredential(
                            rs.getLong("profile_id"),
                            rs.getString("password_hash"),
                            rs.getInt("failed_attempts"),
                            toInstant(rs.getTimestamp("locked_until"))
                    ),
                    profileId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void insert(long profileId, String passwordHash) {
        jdbcTemplate.update(
                """
                INSERT INTO user_password_credential (profile_id, password_hash, password_set_at)
                VALUES (?, ?, NOW(3))
                """,
                profileId,
                passwordHash
        );
    }

    @Override
    public void recordFailedAttempt(long profileId, int failedAttempts, Instant lockedUntil) {
        jdbcTemplate.update(
                """
                UPDATE user_password_credential
                SET failed_attempts = ?, locked_until = ?
                WHERE profile_id = ?
                """,
                failedAttempts,
                lockedUntil == null ? null : Timestamp.from(lockedUntil),
                profileId
        );
    }

    @Override
    public void resetFailedAttempts(long profileId) {
        jdbcTemplate.update(
                """
                UPDATE user_password_credential
                SET failed_attempts = 0, locked_until = NULL
                WHERE profile_id = ?
                """,
                profileId
        );
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
