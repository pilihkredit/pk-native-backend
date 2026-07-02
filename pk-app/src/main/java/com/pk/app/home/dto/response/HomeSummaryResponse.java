package com.pk.app.home.dto.response;

import java.util.List;

/**
 * Post-login user profile summary with latest lender user status.
 */
public record HomeSummaryResponse(
        String partnerUserId,
        String userStage,
        String nextAction,
        String kycStatus,
        List<String> completedModules,
        List<String> missingModules,
        String lenderUserId,
        Integer userLoanLifeTimeStatus,
        Integer userLoanLifeTimeLastAction,
        Long freezeEndTime,
        Integer onLoanCount,
        Long creditContractExpireTime,
        String lastLenderRequestJson,
        String lastLenderResponseJson,
        Long queriedAt
) {
}
