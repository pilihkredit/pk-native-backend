package com.pk.core.repay;

import java.util.List;

public record LenderRepayVa(
        String vaNo,
        String bankCode,
        String bankName,
        Integer bankType,
        String icon,
        List<LenderRepayVaChannel> bankChannels,
        boolean defaultFlag,
        boolean disabled,
        boolean show
) {
    public record LenderRepayVaChannel(
            String bankChannel,
            String instruction,
            boolean defaultChannel
    ) {
    }
}
