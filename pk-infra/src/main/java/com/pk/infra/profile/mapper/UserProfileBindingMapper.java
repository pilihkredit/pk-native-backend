package com.pk.infra.profile.mapper;

import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileBindingMapper {
    int recordLenderProfileSyncWithExternalUser(
            @Param("profileId") long profileId,
            @Param("externalUserId") String externalUserId
    );

    int recordLenderProfileSyncWithoutExternalUser(@Param("profileId") long profileId);

    int updateKycStatus(@Param("profileId") long profileId, @Param("kycStatus") String kycStatus);

    int closeAccount(
            @Param("profileId") long profileId,
            @Param("closedAt") Instant closedAt,
            @Param("retentionUntil") Instant retentionUntil
    );

    AccountClosureRow findClosure(@Param("profileId") long profileId);

    /**
     * Include non-null {@code id} so MyBatis does not discard the row when
     * {@code deleted_at}/{@code retention_until} are both null (active accounts).
     */
    record AccountClosureRow(long id, Instant deletedAt, Instant retentionUntil) {
    }
}
