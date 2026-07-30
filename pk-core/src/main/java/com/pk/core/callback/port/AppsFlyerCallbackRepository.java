package com.pk.core.callback.port;

import java.util.Optional;

public interface AppsFlyerCallbackRepository {
    long insert(AppsFlyerCallbackInsert insert);

    int backfillBinding(String appsflyerId, Long userId, String deviceNo);

    Optional<AppsFlyerCallbackData> findLatestByAppsflyerId(String appsflyerId);

    Optional<AppsFlyerCallbackData> findLatestByAppsflyerIdAndEventName(String appsflyerId, String eventName);

    Optional<AppsFlyerCallbackData> findLatestByAdvertisingId(String advertisingId);

    Optional<AppsFlyerCallbackData> findLatestByAdvertisingIdAndEventName(String advertisingId, String eventName);

    Optional<AppsFlyerCallbackData> findLatestByAndroidId(String androidId);

    Optional<AppsFlyerCallbackData> findLatestByAndroidIdAndEventName(String androidId, String eventName);

    Optional<AppsFlyerCallbackData> findLatestByDeviceNo(String deviceNo);

    Optional<AppsFlyerCallbackData> findLatestByDeviceNoAndEventName(String deviceNo, String eventName);

    Optional<AppsFlyerCallbackData> findLatestByDeviceNoAndConversionType(String deviceNo, String conversionType);

    /** Fields used to fill lender appsFlyerInstall when user_profile_af is blank. */
    record AppsFlyerCallbackData(
            String appsflyerId,
            String advertisingId,
            String androidId,
            String attributedTouchTime,
            String gpClickTime,
            String installTime,
            String mediaSource,
            String afPrt,
            String afAdsetId,
            String afAdset,
            String afSiteid,
            String afCId,
            String campaign,
            String appVersion,
            String appId,
            String deviceType,
            String osVersion,
            String countryCode,
            String city,
            String postalCode,
            String ip,
            String operator,
            String deviceCategory,
            String platform,
            String deviceModel,
            String idfv,
            String idfa,
            String afAd,
            String afChannel,
            String attributedTouchType,
            String afAdId,
            String afAdType,
            String contributor1TouchType,
            String contributor1TouchTime,
            String contributor1AfPrt,
            String contributor1MatchType,
            String contributor1EngagementType,
            String bundleId,
            String matchType,
            String gpInstallBegin,
            String eventName
    ) {
    }

    record AppsFlyerCallbackInsert(
            Long userId,
            String deviceNo,
            String appsflyerId,
            String advertisingId,
            String androidId,
            String attributedTouchTime,
            String attributedTouchTimeSelectedTimezone,
            String gpClickTime,
            String installTime,
            String mediaSource,
            String afPrt,
            String afAdsetId,
            String afAdset,
            String afSiteid,
            String afCId,
            String campaign,
            String appVersion,
            String appId,
            String deviceType,
            String osVersion,
            String countryCode,
            String city,
            String postalCode,
            String ip,
            String operator,
            String deviceCategory,
            String platform,
            String deviceModel,
            String idfv,
            String idfa,
            String afAd,
            String afChannel,
            String attributedTouchType,
            String afAdId,
            String afAdType,
            String contributor1TouchType,
            String contributor1TouchTime,
            String contributor1AfPrt,
            String contributor1MatchType,
            String contributor1EngagementType,
            String bundleId,
            String matchType,
            String gpInstallBegin,
            String eventSource,
            String eventTime,
            String eventTimeSelectedTimezone,
            String appName,
            String appType,
            String campaignType,
            String conversionType,
            String engagementType,
            String afAttributionLookback,
            Boolean isRetargeting,
            String region,
            String state,
            String dma,
            Boolean wifi,
            String carrier,
            String language,
            String installTimeSelectedTimezone,
            String deviceDownloadTime,
            String deviceDownloadTimeSelectedTimezone,
            String gpReferrer,
            String sdkVersion,
            String apiVersion,
            String userAgent,
            String selectedTimezone,
            String selectedCurrency,
            Boolean isLat,
            String att,
            String originalUrl,
            String httpReferrer,
            String eventValue,
            String eventValueAppId,
            String eventName,
            String eventType,
            String customerUserId,
            String rawData,
            String callbackStatus,
            String errorMessage
    ) {
    }
}
