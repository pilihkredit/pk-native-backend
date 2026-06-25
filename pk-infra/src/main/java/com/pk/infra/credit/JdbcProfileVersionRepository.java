package com.pk.infra.credit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.port.ProfileVersionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProfileVersionRepository implements ProfileVersionRepository {
    private static final String LEGAL_BASIS = "CONTRACT";
    private static final String PROCESSING_PURPOSE = "CREDIT_APPLY";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcProfileVersionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public long createSnapshot(long profileId, List<String> completedModules, String source) {
        int versionNo = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(MAX(version_no), 0) + 1
                FROM user_profile_version
                WHERE profile_id = ?
                """,
                Integer.class,
                profileId
        );
        String completedModulesJson = serializeModules(completedModules);
        String snapshotJson = "{\"profileId\":" + profileId + "}";
        String snapshotHash = sha256Hex(snapshotJson);
        jdbcTemplate.update(
                """
                INSERT INTO user_profile_version (
                    profile_id,
                    version_no,
                    snapshot_hash,
                    snapshot_json,
                    completed_modules,
                    legal_basis,
                    processing_purpose,
                    source
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                profileId,
                versionNo,
                snapshotHash,
                snapshotJson,
                completedModulesJson,
                LEGAL_BASIS,
                PROCESSING_PURPOSE,
                source
        );
        Long id = jdbcTemplate.queryForObject(
                """
                SELECT id
                FROM user_profile_version
                WHERE profile_id = ? AND version_no = ?
                """,
                Long.class,
                profileId,
                versionNo
        );
        if (id == null) {
            throw new IllegalStateException("Failed to load inserted profile version");
        }
        return id;
    }

    private String serializeModules(List<String> completedModules) {
        try {
            return objectMapper.writeValueAsString(completedModules);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize completed modules", exception);
        }
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
