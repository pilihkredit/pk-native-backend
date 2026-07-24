package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileLoginLogRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileLoginLogMapper {
    ProfileLoginLogRow findByProfileId(@Param("profileId") long profileId);

    int upsert(ProfileLoginLogRow row);

    int updateLastLenderInteraction(@Param("profileId") long profileId, @Param("externalInteractionId") Long externalInteractionId);
}
