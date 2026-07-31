package com.pk.infra.profile.mapper;

import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileBindingMapper {
    int recordLenderProfileSyncWithExternalUser(
            @Param("userId") long userId,
            @Param("externalUserId") String externalUserId
    );

    int recordLenderProfileSyncWithoutExternalUser(@Param("userId") long userId);

    int updateKycStatus(@Param("userId") long userId, @Param("kycStatus") String kycStatus);

    int closeAccount(
            @Param("userId") long userId,
            @Param("closedAt") Instant closedAt,
            @Param("retentionUntil") Instant retentionUntil
    );

    AccountClosureRow findClosure(@Param("userId") long userId);

    /**
     * Include non-null {@code id} so MyBatis does not discard the row when
     * {@code deleted_at}/{@code retention_until} are both null (active accounts).
     */
    record AccountClosureRow(long id, Instant deletedAt, Instant retentionUntil) {
    }
}
