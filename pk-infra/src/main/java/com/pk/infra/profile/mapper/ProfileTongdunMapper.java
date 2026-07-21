package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileTongdunRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileTongdunMapper {
    ProfileTongdunRow findByRequestId(@Param("requestId") String requestId);

    int insert(ProfileTongdunRow row);

    int updateLastLenderAudit(
            @Param("requestId") String requestId,
            @Param("requestDataJson") String requestDataJson,
            @Param("responseDataJson") String responseDataJson
    );
}
