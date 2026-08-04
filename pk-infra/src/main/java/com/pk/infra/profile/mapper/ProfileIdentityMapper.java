package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileIdentityRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileIdentityMapper {
    ProfileIdentityRow findByUserId(@Param("userId") long userId);

    int upsert(ProfileIdentityRow row);

    int updateLastLenderInteraction(
            @Param("userId") long userId,
            @Param("externalInteractionId") Long externalInteractionId
    );

}
