package com.pk.infra.accountclosure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountClosureMapper {
    int countActiveDeletionQueue(@Param("userId") long userId);

    int insertDeletionQueue(
            @Param("userId") long userId,
            @Param("partnerUserId") String partnerUserId,
            @Param("mobileNo") String mobileNo,
            @Param("reason") String reason
    );
}
