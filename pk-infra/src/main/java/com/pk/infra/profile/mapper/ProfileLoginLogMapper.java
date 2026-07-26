package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileLoginLogRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileLoginLogMapper {
    ProfileLoginLogRow findByUserId(@Param("userId") long userId);

    int upsert(ProfileLoginLogRow row);

    int updateLastLenderInteraction(@Param("userId") long userId, @Param("externalInteractionId") Long externalInteractionId);
}
