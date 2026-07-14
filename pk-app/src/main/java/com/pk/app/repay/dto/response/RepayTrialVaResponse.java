package com.pk.app.repay.dto.response;

import com.pk.core.repay.LenderRepayVa;
import java.util.List;

public record RepayTrialVaResponse(
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
    public static RepayTrialVaResponse from(LenderRepayVa va) {
        if (va == null) {
            return null;
        }
        List<RepayVaChannelResponse> channels = va.bankChannels() == null
                ? List.of()
                : va.bankChannels().stream()
                        .map(channel -> new RepayVaChannelResponse(
                                channel.bankChannel(),
                                channel.instruction(),
                                channel.defaultChannel()
                        ))
                        .toList();
        return new RepayTrialVaResponse(
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
