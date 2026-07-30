package com.pk.infra.review;

import com.pk.core.review.ReviewSandboxLoanDataPort;
import com.pk.infra.review.mapper.ReviewSandboxLoanDataMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewSandboxLoanDataRepository implements ReviewSandboxLoanDataPort {
    private final ReviewSandboxLoanDataMapper mapper;

    public ReviewSandboxLoanDataRepository(ReviewSandboxLoanDataMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ReviewLoanSnapshot> findByLoanApplyId(String loanApplyId) {
        return Optional.ofNullable(mapper.findByLoanApplyId(loanApplyId));
    }

    @Override
    public List<ReviewLoanSnapshot> findDisbursedByPartnerUserId(String partnerUserId) {
        return mapper.findDisbursedByPartnerUserId(partnerUserId);
    }

    @Override
    public boolean hasApprovedCredit(String mobileNo) {
        return mapper.hasApprovedCredit(mobileNo);
    }

    @Override
    public boolean hasDisbursedLoan(String mobileNo) {
        return mapper.hasDisbursedLoan(mobileNo);
    }
}
