package com.pk.infra.loan.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface LoanStatusHistoryMapper {
    int insert(@Param("loanApplicationId") long loanApplicationId, @Param("fromStatus") String fromStatus,
        @Param("toStatus") String toStatus, @Param("externalStatus") String externalStatus, @Param("source") String source);
}
