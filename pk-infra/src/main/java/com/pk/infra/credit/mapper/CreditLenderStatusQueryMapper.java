package com.pk.infra.credit.mapper;

import com.pk.core.credit.port.CreditLenderStatusQueryRepository.CreditLenderStatusQueryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CreditLenderStatusQueryMapper {
    int insert(CreditLenderStatusQueryData data);

    CreditLenderStatusQueryData findLatestByApplyId(@Param("applyId") String applyId);

    CreditLenderStatusQueryData findLatestByApplyIdAndUserId(
            @Param("applyId") String applyId,
            @Param("userId") long userId
    );
}
