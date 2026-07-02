package com.pk.infra.credit.mapper;

import com.pk.core.credit.port.CreditLenderStatusQueryRepository.CreditLenderStatusQueryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CreditLenderStatusQueryMapper {
    int upsert(CreditLenderStatusQueryData data);

    CreditLenderStatusQueryData findByApplyId(@Param("applyId") String applyId);

    CreditLenderStatusQueryData findByApplyIdAndProfileId(
            @Param("applyId") String applyId,
            @Param("profileId") long profileId
    );
}
