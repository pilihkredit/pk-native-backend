package com.pk.infra.repay.mapper;

import com.pk.core.repay.port.LoanLenderBillRepository.LoanLenderBillData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanLenderBillMapper {
    int upsert(LoanLenderBillData data);

    List<LoanLenderBillData> findByUserIdAndBillStatuses(
            @Param("userId") long userId,
            @Param("billStatuses") List<String> billStatuses
    );
}
