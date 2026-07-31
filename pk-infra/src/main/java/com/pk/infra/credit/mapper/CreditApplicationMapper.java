package com.pk.infra.credit.mapper;

import com.pk.core.credit.port.CreditApplicationRepository.CreditApplicationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CreditApplicationMapper {
    CreditApplicationRecord findById(@Param("id") long id);

    CreditApplicationRecord findByRequestId(@Param("requestId") String requestId);

    CreditApplicationRecord findByApplyIdAndUserId(
            @Param("applyId") String applyId,
            @Param("userId") long userId
    );

    CreditApplicationRecord findLatestByUserId(@Param("userId") long userId);

    CreditApplicationRecord findByApplyId(@Param("applyId") String applyId);

    int insert(com.pk.infra.credit.repository.CreditApplicationRepositoryImpl.CreditApplicationInsertParam param);

    Long findIdByApplyId(@Param("applyId") String applyId);

    int updateApplyNo(@Param("id") long id, @Param("applyNo") String applyNo);

    int updateLastLenderInteraction(
            @Param("id") long id,
            @Param("externalInteractionId") Long externalInteractionId
    );
}
