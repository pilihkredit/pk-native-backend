package com.pk.infra.profile;

import com.pk.core.callback.port.AppsFlyerCallbackRepository.AppsFlyerCallbackData;
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.sync.ProfileSyncPayload;

/** Maps stored AF rows to lender sync payload; blank fields filled from Push callback. */
final class AppsFlyerPayloadMapper {
    private AppsFlyerPayloadMapper() {
    }

    static ProfileSyncPayload.AppsFlyerInstallPayload toPayload(ProfileAfData data) {
        return toPayload(data, null);
    }

    static ProfileSyncPayload.AppsFlyerInstallPayload toPayload(ProfileAfData data, AppsFlyerCallbackData callback) {
        return new ProfileSyncPayload.AppsFlyerInstallPayload(
                prefer(data.appsflyerId(), callback == null ? null : callback.appsflyerId()),
                prefer(data.advertisingId(), callback == null ? null : callback.advertisingId()),
                prefer(data.androidId(), callback == null ? null : callback.androidId()),
                prefer(data.attributedTouchTime(), callback == null ? null : callback.attributedTouchTime()),
                prefer(data.gpClickTime(), callback == null ? null : callback.gpClickTime()),
                prefer(data.installTime(), callback == null ? null : callback.installTime()),
                prefer(data.mediaSource(), callback == null ? null : callback.mediaSource()),
                prefer(data.afPrt(), callback == null ? null : callback.afPrt()),
                prefer(data.afAdsetId(), callback == null ? null : callback.afAdsetId()),
                prefer(data.afAdset(), callback == null ? null : callback.afAdset()),
                prefer(data.afSiteid(), callback == null ? null : callback.afSiteid()),
                prefer(data.afCId(), callback == null ? null : callback.afCId()),
                prefer(data.campaign(), callback == null ? null : callback.campaign()),
                prefer(data.appVersion(), callback == null ? null : callback.appVersion()),
                prefer(data.appId(), callback == null ? null : callback.appId()),
                prefer(data.deviceType(), callback == null ? null : callback.deviceType()),
                prefer(data.osVersion(), callback == null ? null : callback.osVersion()),
                prefer(data.countryCode(), callback == null ? null : callback.countryCode()),
                prefer(data.city(), callback == null ? null : callback.city()),
                prefer(data.postalCode(), callback == null ? null : callback.postalCode()),
                prefer(data.ip(), callback == null ? null : callback.ip()),
                prefer(data.operator(), callback == null ? null : callback.operator()),
                prefer(data.deviceCategory(), callback == null ? null : callback.deviceCategory()),
                prefer(data.platform(), callback == null ? null : callback.platform()),
                prefer(data.deviceModel(), callback == null ? null : callback.deviceModel()),
                prefer(data.idfv(), callback == null ? null : callback.idfv()),
                prefer(data.idfa(), callback == null ? null : callback.idfa()),
                prefer(data.afAd(), callback == null ? null : callback.afAd()),
                prefer(data.afChannel(), callback == null ? null : callback.afChannel()),
                prefer(data.attributedTouchType(), callback == null ? null : callback.attributedTouchType()),
                prefer(data.afAdId(), callback == null ? null : callback.afAdId()),
                prefer(data.afAdType(), callback == null ? null : callback.afAdType()),
                prefer(data.contributor1TouchType(), callback == null ? null : callback.contributor1TouchType()),
                prefer(data.contributor1TouchTime(), callback == null ? null : callback.contributor1TouchTime()),
                prefer(data.contributor1AfPrt(), callback == null ? null : callback.contributor1AfPrt()),
                prefer(data.contributor1MatchType(), callback == null ? null : callback.contributor1MatchType()),
                prefer(data.contributor1EngagementType(), callback == null ? null : callback.contributor1EngagementType()),
                prefer(data.bundleId(), callback == null ? null : callback.bundleId()),
                prefer(data.matchType(), callback == null ? null : callback.matchType()),
                prefer(data.gpInstallBegin(), callback == null ? null : callback.gpInstallBegin())
        );
    }

    static ProfileSyncPayload.AppsFlyerInstallPayload fromCallback(AppsFlyerCallbackData callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback is required");
        }
        return new ProfileSyncPayload.AppsFlyerInstallPayload(
                blankToNull(callback.appsflyerId()),
                blankToNull(callback.advertisingId()),
                blankToNull(callback.androidId()),
                blankToNull(callback.attributedTouchTime()),
                blankToNull(callback.gpClickTime()),
                blankToNull(callback.installTime()),
                blankToNull(callback.mediaSource()),
                blankToNull(callback.afPrt()),
                blankToNull(callback.afAdsetId()),
                blankToNull(callback.afAdset()),
                blankToNull(callback.afSiteid()),
                blankToNull(callback.afCId()),
                blankToNull(callback.campaign()),
                blankToNull(callback.appVersion()),
                blankToNull(callback.appId()),
                blankToNull(callback.deviceType()),
                blankToNull(callback.osVersion()),
                blankToNull(callback.countryCode()),
                blankToNull(callback.city()),
                blankToNull(callback.postalCode()),
                blankToNull(callback.ip()),
                blankToNull(callback.operator()),
                blankToNull(callback.deviceCategory()),
                blankToNull(callback.platform()),
                blankToNull(callback.deviceModel()),
                blankToNull(callback.idfv()),
                blankToNull(callback.idfa()),
                blankToNull(callback.afAd()),
                blankToNull(callback.afChannel()),
                blankToNull(callback.attributedTouchType()),
                blankToNull(callback.afAdId()),
                blankToNull(callback.afAdType()),
                blankToNull(callback.contributor1TouchType()),
                blankToNull(callback.contributor1TouchTime()),
                blankToNull(callback.contributor1AfPrt()),
                blankToNull(callback.contributor1MatchType()),
                blankToNull(callback.contributor1EngagementType()),
                blankToNull(callback.bundleId()),
                blankToNull(callback.matchType()),
                blankToNull(callback.gpInstallBegin())
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String prefer(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }
}
