package com.pk.infra.auth.mapper;

import com.pk.core.auth.port.UserPasswordCredentialRepository.PasswordCredential;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserPasswordCredentialMapper {
    int countByProfileId(@Param("profileId") long profileId);

    PasswordCredential findByProfileId(@Param("profileId") long profileId);

    int insert(@Param("profileId") long profileId, @Param("passwordHash") String passwordHash);

    int recordFailedAttempt(
            @Param("profileId") long profileId,
            @Param("failedAttempts") int failedAttempts,
            @Param("lockedUntil") Instant lockedUntil
    );

    int resetFailedAttempts(@Param("profileId") long profileId);
}
