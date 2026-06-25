package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayVaFacade;

public record RepayVaInfoResponse(
        String vaNo,
        String bankChannel,
        String bankName,
        boolean defaultFlag,
        String status
) {
    public static RepayVaInfoResponse from(RepayVaFacade.VaInfoResult va) {
        return new RepayVaInfoResponse(
                va.vaNo(),
                va.bankChannel(),
                va.bankName(),
                va.defaultFlag(),
                va.status()
        );
    }
}
