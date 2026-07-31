package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayTrialFacade;

public record RepayTrialVaSummaryResponse(
        String vaNo,
        String bankChannel,
        String bankName,
        boolean defaultFlag
) {
    public static RepayTrialVaSummaryResponse from(RepayTrialFacade.VaSummary va) {
        return new RepayTrialVaSummaryResponse(va.vaNo(), va.bankChannel(), va.bankName(), va.defaultFlag());
    }
}
