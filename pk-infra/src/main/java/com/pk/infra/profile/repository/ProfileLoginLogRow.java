package com.pk.infra.profile.repository;

import java.math.BigDecimal;

public record ProfileLoginLogRow(
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
