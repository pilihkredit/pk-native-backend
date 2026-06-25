package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayVaFacade;
import java.util.List;

public record RepayVaListResponse(List<RepayVaInfoResponse> vaList) {
    public static RepayVaListResponse from(RepayVaFacade.VaListResult result) {
        return new RepayVaListResponse(result.vaList().stream().map(RepayVaInfoResponse::from).toList());
    }
}
