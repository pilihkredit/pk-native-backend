package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileIdentityRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileIdentityMapper {
    ProfileIdentityRow findByProfileId(@Param("profileId") long profileId);

    int upsert(ProfileIdentityRow row);

    int updateLastLenderAudit(
            @Param("profileId") long profileId,
            @Param("requestJson") String requestJson,
            @Param("responseJson") String responseJson
    );
}
