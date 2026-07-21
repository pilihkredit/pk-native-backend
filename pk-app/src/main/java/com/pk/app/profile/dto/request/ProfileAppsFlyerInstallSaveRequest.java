package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * AppsFlyer install payload; syncs lender {@code userInfo.appsFlyerInstall}.
 */
public record ProfileAppsFlyerInstallSaveRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 64) String appsflyerId,
        @Size(max = 128) String advertisingId,
        @Size(max = 128) String androidId,
        @Size(max = 32) String attributedTouchTime,
        @Size(max = 32) String gpClickTime,
        @Size(max = 32) String installTime,
        @Size(max = 128) String mediaSource,
        @Size(max = 128) String afPrt,
        @Size(max = 128) String afAdsetId,
        @Size(max = 128) String afAdset,
        @Size(max = 128) String afSiteid,
        @Size(max = 128) String afCId,
        @Size(max = 128) String campaign,
        @Size(max = 64) String appVersion,
        @Size(max = 128) String appId,
        @Size(max = 128) String deviceType,
        @Size(max = 64) String osVersion,
        @Size(max = 16) String countryCode,
        @Size(max = 128) String city,
        @Size(max = 32) String postalCode,
        @Size(max = 64) String ip,
        @Size(max = 128) String operator,
        @Size(max = 64) String deviceCategory,
        @Size(max = 32) String platform,
        @Size(max = 128) String deviceModel,
        @Size(max = 128) String idfv,
        @Size(max = 128) String idfa,
        @Size(max = 128) String afAd,
        @Size(max = 128) String afChannel,
        @Size(max = 64) String attributedTouchType,
        @Size(max = 128) String afAdId,
        @Size(max = 64) String afAdType,
        @Size(max = 64) String contributor1TouchType,
        @Size(max = 32) String contributor1TouchTime,
        @Size(max = 128) String contributor1AfPrt,
        @Size(max = 64) String contributor1MatchType,
        @Size(max = 64) String contributor1EngagementType,
        @Size(max = 128) String bundleId,
        @Size(max = 64) String matchType,
        @Size(max = 32) String gpInstallBegin,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
