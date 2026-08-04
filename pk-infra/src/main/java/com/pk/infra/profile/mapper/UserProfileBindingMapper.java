package com.pk.infra.profile.mapper;

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

}
