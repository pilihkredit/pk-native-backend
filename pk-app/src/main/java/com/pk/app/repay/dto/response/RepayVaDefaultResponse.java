package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayVaFacade;

public record RepayVaDefaultResponse(String vaNo, boolean defaultFlag) {
    public static RepayVaDefaultResponse from(RepayVaFacade.VaDefaultResult result) {
        return new RepayVaDefaultResponse(result.vaNo(), result.defaultFlag());
    }
}
