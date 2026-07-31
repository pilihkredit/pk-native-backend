package com.pk.infra.loan.mapper;

import com.pk.core.loan.port.LoanLenderHistoryOrderRepository.LoanLenderHistoryOrderData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanLenderHistoryOrderMapper {
    int upsert(LoanLenderHistoryOrderData data);

    List<LoanLenderHistoryOrderData> findByUserId(@Param("userId") long userId);
}
