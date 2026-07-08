package com.pk.app.home.dto.response;

/**
 * Lender user status returned by {@code POST /api/open/v1/user/status}.
 */
public record HomeSummaryResponse(
        String partnerUserId,
        String userId,
        Integer userLoanLifeTimeStatus,
        Integer userLoanLifeTimeLastAction,
        Long freezeEndTime,
        Integer onLoanCount,
        Long creditContractExpireTime,
        Boolean autoCredit
) {
}
