package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileTongdunRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileTongdunMapper {
    ProfileTongdunRow findByRequestId(@Param("requestId") String requestId);

    int insert(ProfileTongdunRow row);

    int updateLastLenderInteraction(@Param("requestId") String requestId, @Param("externalInteractionId") Long externalInteractionId);
}
