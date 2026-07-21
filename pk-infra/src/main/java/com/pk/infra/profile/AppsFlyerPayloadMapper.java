package com.pk.infra.profile;

import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.sync.ProfileSyncPayload;

/** Maps stored AF rows to lender sync payload. */
final class AppsFlyerPayloadMapper {
    private AppsFlyerPayloadMapper() {
    }

    static ProfileSyncPayload.AppsFlyerInstallPayload toPayload(ProfileAfData data) {
        return new ProfileSyncPayload.AppsFlyerInstallPayload(
                data.appsflyerId(),
                data.advertisingId(),
                data.androidId(),
                data.attributedTouchTime(),
                data.gpClickTime(),
                data.installTime(),
                data.mediaSource(),
                data.afPrt(),
                data.afAdsetId(),
                data.afAdset(),
                data.afSiteid(),
                data.afCId(),
                data.campaign(),
                data.appVersion(),
                data.appId(),
                data.deviceType(),
                data.osVersion(),
                data.countryCode(),
                data.city(),
                data.postalCode(),
                data.ip(),
                data.operator(),
                data.deviceCategory(),
                data.platform(),
                data.deviceModel(),
                data.idfv(),
                data.idfa(),
                data.afAd(),
                data.afChannel(),
                data.attributedTouchType(),
                data.afAdId(),
                data.afAdType(),
                data.contributor1TouchType(),
                data.contributor1TouchTime(),
                data.contributor1AfPrt(),
                data.contributor1MatchType(),
                data.contributor1EngagementType(),
                data.bundleId(),
                data.matchType(),
                data.gpInstallBegin()
        );
    }
}
