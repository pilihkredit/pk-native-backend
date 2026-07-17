package com.pk.core.profile;

import java.math.BigDecimal;

public record ProfileLoginLogData(
        long profileId,
        String mobileNo,
        int loginType,
        String loginIp,
        BigDecimal loginLat,
        BigDecimal loginLng,
        String moduleStatus,
        String lastRequestId,
        String lastLenderRequestJson,
        String lastLenderResponseJson
) {
}
