package com.pk.infra.home.mapper;

import com.pk.core.home.port.UserLenderStatusQueryRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserLenderStatusQueryMapper {
    int upsert(UserLenderStatusQueryRepository.UserLenderStatusQueryData data);

    UserLenderStatusQueryRepository.UserLenderStatusQueryData findByProfileId(@Param("profileId") long profileId);
}
