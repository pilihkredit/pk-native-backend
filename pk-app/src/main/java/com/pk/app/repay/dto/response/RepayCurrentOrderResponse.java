package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayCurrentOrderFacade;

public record RepayCurrentOrderResponse(String requestId, String status) {
    public static RepayCurrentOrderResponse from(RepayCurrentOrderFacade.CurrentOrderResult result) {
        return new RepayCurrentOrderResponse(result.requestId(), result.status());
    }
}
