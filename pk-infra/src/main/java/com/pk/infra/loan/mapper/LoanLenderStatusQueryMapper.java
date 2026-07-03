package com.pk.infra.loan.mapper;

import com.pk.core.loan.port.LoanLenderStatusQueryRepository.LoanLenderStatusQueryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanLenderStatusQueryMapper {
    int upsert(LoanLenderStatusQueryData data);

    LoanLenderStatusQueryData findByLoanApplyId(@Param("loanApplyId") String loanApplyId);

    LoanLenderStatusQueryData findByLoanApplyIdAndProfileId(
            @Param("loanApplyId") String loanApplyId,
            @Param("profileId") long profileId
    );
}
