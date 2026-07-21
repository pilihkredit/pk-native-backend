package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileAfRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileAfMapper {
    ProfileAfRow findByRequestId(@Param("requestId") String requestId);

    int insert(ProfileAfRow row);

    int updateLastLenderAudit(
            @Param("requestId") String requestId,
            @Param("requestDataJson") String requestDataJson,
            @Param("responseDataJson") String responseDataJson
    );
}
