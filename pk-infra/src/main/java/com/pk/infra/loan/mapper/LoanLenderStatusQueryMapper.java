package com.pk.infra.loan.mapper;

import com.pk.core.loan.port.LoanLenderStatusQueryRepository.LoanLenderStatusQueryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanLenderStatusQueryMapper {
    int insert(LoanLenderStatusQueryData data);

    LoanLenderStatusQueryData findLatestByLoanApplyId(@Param("loanApplyId") String loanApplyId);

    LoanLenderStatusQueryData findLatestByLoanApplyIdAndUserId(
            @Param("loanApplyId") String loanApplyId,
            @Param("userId") long userId
    );
}
