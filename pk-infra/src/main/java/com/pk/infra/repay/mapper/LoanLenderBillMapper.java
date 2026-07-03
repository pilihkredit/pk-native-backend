package com.pk.infra.repay.mapper;

import com.pk.core.repay.port.LoanLenderBillRepository.LoanLenderBillData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanLenderBillMapper {
    int upsert(LoanLenderBillData data);

    List<LoanLenderBillData> findByProfileIdAndBillStatuses(
            @Param("profileId") long profileId,
            @Param("billStatuses") List<String> billStatuses
    );
}
