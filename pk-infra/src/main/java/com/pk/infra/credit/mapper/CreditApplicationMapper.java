package com.pk.infra.credit.mapper;

import com.pk.core.credit.port.CreditApplicationRepository.CreditApplicationRecord;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CreditApplicationMapper {
    CreditApplicationRecord findById(@Param("id") long id);

    CreditApplicationRecord findByRequestId(@Param("requestId") String requestId);

    CreditApplicationRecord findByApplyIdAndProfileId(
            @Param("applyId") String applyId,
            @Param("profileId") long profileId
    );

    CreditApplicationRecord findByApplyId(@Param("applyId") String applyId);

    int insert(com.pk.infra.credit.repository.CreditApplicationRepositoryImpl.CreditApplicationInsertParam param);

    Long findIdByApplyId(@Param("applyId") String applyId);

    int updateStatus(
            @Param("id") long id,
            @Param("status") String status,
            @Param("externalStatus") String externalStatus,
            @Param("lastErrorCode") String lastErrorCode
    );

    int markSubmitted(
            @Param("id") long id,
            @Param("externalCreditApplyNo") String externalCreditApplyNo,
            @Param("externalStatus") String externalStatus
    );

    int scheduleNextPoll(@Param("id") long id, @Param("nextPollAt") Instant nextPollAt);

    int updateFreezeEndAt(@Param("id") long id, @Param("freezeEndAt") Instant freezeEndAt);

    List<CreditApplicationRecord> findDueForPoll(@Param("limit") int limit);
}
