package com.pk.infra.credit.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.infra.credit.mapper.ProfileVersionMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileVersionRepositoryImpl implements ProfileVersionRepository {
    private static final String LEGAL_BASIS = "CONTRACT";
    private static final String PROCESSING_PURPOSE = "CREDIT_APPLY";

    private final ProfileVersionMapper profileVersionMapper;
    private final ObjectMapper objectMapper;

    public ProfileVersionRepositoryImpl(ProfileVersionMapper profileVersionMapper, ObjectMapper objectMapper) {
        this.profileVersionMapper = profileVersionMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public long createSnapshot(long userId, String mobileNo, List<String> completedModules, String source) {
        int versionNo = profileVersionMapper.nextVersionNo(userId);
        String completedModulesJson = serializeModules(completedModules);
        String snapshotJson = "{\"userId\":" + userId + "}";
        String snapshotHash = sha256Hex(snapshotJson);
        profileVersionMapper.insert(
                userId,
                mobileNo,
                versionNo,
                snapshotHash,
                snapshotJson,
                completedModulesJson,
                LEGAL_BASIS,
                PROCESSING_PURPOSE,
                source
        );
        Long id = profileVersionMapper.findIdByUserIdAndVersionNo(userId, versionNo);
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
