package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileAfRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileAfMapper {
    ProfileAfRow findByRequestId(@Param("requestId") String requestId);

    ProfileAfRow findLatestByDeviceNo(@Param("deviceNo") String deviceNo);

    int insert(ProfileAfRow row);

    int updateLastLenderInteraction(@Param("requestId") String requestId, @Param("externalInteractionId") Long externalInteractionId);

    int bindProfileIfNull(
            @Param("id") long id,
            @Param("userId") long userId
    );
}
