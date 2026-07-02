package com.pk.infra.credit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CreditStatusHistoryMapper {
    int insert(
            @Param("creditApplicationId") long creditApplicationId,
            @Param("mobileNo") String mobileNo,
            @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus,
            @Param("externalStatus") String externalStatus,
            @Param("source") String source
    );
}
