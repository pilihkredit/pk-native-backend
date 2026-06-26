package com.pk.app.credit.dto.request;

import jakarta.validation.constraints.Size;

public record CreditAppInfoRequest(
        @Size(max = 128) String appName,
        @Size(max = 128) String packageName,
        Integer appFlags,
        Integer appType,
        @Size(max = 64) String versionCode,
        @Size(max = 128) String versionName,
        Long inTime,
        Long upTime
) {
}
