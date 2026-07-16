package com.pk.infra.profile.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface UserProfileBindingMapper {
    int recordLenderProfileSyncWithExternalUser(@Param("profileId") long profileId, @Param("externalUserId") String externalUserId);
    int recordLenderProfileSyncWithoutExternalUser(@Param("profileId") long profileId);
    int updateKycStatus(@Param("profileId") long profileId, @Param("kycStatus") String kycStatus);
    int closeAccount(
            @Param("profileId") long profileId,
            @Param("closedAt") java.time.Instant closedAt,
            @Param("retentionUntil") java.time.Instant retentionUntil
    );
}
