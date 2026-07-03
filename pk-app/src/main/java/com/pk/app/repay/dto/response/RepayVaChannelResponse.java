package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayVaFacade;

public record RepayVaChannelResponse(
        String bankChannel,
        String instruction,
        boolean defaultChannel
) {
    public static RepayVaChannelResponse from(RepayVaFacade.VaChannelResult channel) {
        return new RepayVaChannelResponse(
                channel.bankChannel(),
                channel.instruction(),
                channel.defaultChannel()
        );
    }
}
