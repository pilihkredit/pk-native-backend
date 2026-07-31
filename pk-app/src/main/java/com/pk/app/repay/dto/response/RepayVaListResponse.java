package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayVaFacade;
import java.util.List;

/**
 * Full lender {@code /repay/va/list} payload for the client.
 */
public record RepayVaListResponse(
        String partnerUserId,
        String userId,
        RepayVaInfoResponse defaultVa,
        List<RepayVaInfoResponse> vas
) {
    public static RepayVaListResponse from(RepayVaFacade.VaListResult result) {
        return new RepayVaListResponse(
                result.partnerUserId(),
                result.userId(),
                result.defaultVa() == null ? null : RepayVaInfoResponse.from(result.defaultVa()),
                result.vas() == null
                        ? List.of()
                        : result.vas().stream().map(RepayVaInfoResponse::from).toList()
        );
    }
}
