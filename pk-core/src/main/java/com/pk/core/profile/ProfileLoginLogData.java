package com.pk.core.profile;

import java.math.BigDecimal;

public record ProfileLoginLogData(
        long userId,
int loginType,
        String loginIp,
        BigDecimal loginLat,
        BigDecimal loginLng,
        String moduleStatus,
        String lastRequestId,
        Long externalInteractionId
) {
}
