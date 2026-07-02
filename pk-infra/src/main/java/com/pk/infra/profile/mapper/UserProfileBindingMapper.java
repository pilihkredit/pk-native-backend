package com.pk.infra.profile.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface UserProfileBindingMapper {
    int recordLenderProfileSyncWithExternalUser(@Param("profileId") long profileId, @Param("externalUserId") String externalUserId);
    int recordLenderProfileSyncWithoutExternalUser(@Param("profileId") long profileId);
    int updateKycStatus(@Param("profileId") long profileId, @Param("kycStatus") String kycStatus);
}
