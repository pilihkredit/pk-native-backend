package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayVaFacade;
import java.util.List;

public record RepayVaInfoResponse(
        String vaNo,
        String bankCode,
        String bankName,
        Integer bankType,
        String icon,
        List<RepayVaChannelResponse> bankChannels,
        boolean defaultFlag,
        boolean disabled,
        boolean show
) {
    public static RepayVaInfoResponse from(RepayVaFacade.VaInfoResult va) {
        List<RepayVaChannelResponse> channels = va.bankChannels() == null
                ? List.of()
                : va.bankChannels().stream().map(RepayVaChannelResponse::from).toList();
        return new RepayVaInfoResponse(
                va.vaNo(),
                va.bankCode(),
                va.bankName(),
                va.bankType(),
                va.icon(),
                channels,
                va.defaultFlag(),
                va.disabled(),
                va.show()
        );
    }
}
