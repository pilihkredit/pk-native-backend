package com.pk.core.credit;

public record CreditRiskAppInfo(
        String appName,
        String packageName,
        Integer appFlags,
        Integer appType,
        String versionCode,
        String versionName,
        Long inTime,
        Long upTime
) {
}
