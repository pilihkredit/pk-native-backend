package com.pk.infra.review.mapper;

import com.pk.core.review.ReviewSandboxLoanDataPort.ReviewLoanSnapshot;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewSandboxLoanDataMapper {
    ReviewLoanSnapshot findByLoanApplyId(@Param("loanApplyId") String loanApplyId);

    List<ReviewLoanSnapshot> findDisbursedByPartnerUserId(@Param("partnerUserId") String partnerUserId);

    boolean hasApprovedCredit(@Param("mobileNo") String mobileNo);

    boolean hasDisbursedLoan(@Param("mobileNo") String mobileNo);
}
